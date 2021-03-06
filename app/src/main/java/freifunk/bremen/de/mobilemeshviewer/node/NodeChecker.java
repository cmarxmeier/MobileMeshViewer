package freifunk.bremen.de.mobilemeshviewer.node;

import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import freifunk.bremen.de.mobilemeshviewer.Checkable;
import freifunk.bremen.de.mobilemeshviewer.PreferenceController;
import freifunk.bremen.de.mobilemeshviewer.api.FreifunkRestConsumer;
import freifunk.bremen.de.mobilemeshviewer.api.manager.RetrofitServiceManager;
import freifunk.bremen.de.mobilemeshviewer.event.NodeListUpdatedEvent;
import freifunk.bremen.de.mobilemeshviewer.event.NodeStatusChangedEvent;
import freifunk.bremen.de.mobilemeshviewer.node.model.detail.NodeDetail;
import freifunk.bremen.de.mobilemeshviewer.node.model.simple.Node;
import freifunk.bremen.de.mobilemeshviewer.node.model.simple.NodeList;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
public class NodeChecker implements Checkable<Node> {

    private PreferenceController preferenceController;
    private RetrofitServiceManager retrofitServiceManager;
    private Optional<List<Node>> currentNodeListOptional = Optional.absent();
    private Optional<List<Node>> currentGWListOptional = Optional.absent();

    @Inject
    public NodeChecker(PreferenceController preferenceController, RetrofitServiceManager retrofitServiceManager) {
        this.preferenceController = preferenceController;
        this.retrofitServiceManager = retrofitServiceManager;
    }

    @Override
    public List<Node> fetchList() {
        return currentNodeListOptional.or(Lists.<Node>newArrayList());
    }

    public List<Node> fetchGWList() {
        return currentGWListOptional.or(Lists.<Node>newArrayList());
    }

    @Override
    public void reloadList() {
        final List<Node> newNodeList = loadList();
        if (!newNodeList.isEmpty()) {
            currentNodeListOptional = Optional.of(justNodes(newNodeList));
            currentGWListOptional = Optional.of(justVPNs(newNodeList));
            Collections.sort(currentNodeListOptional.get());
            Collections.sort(currentGWListOptional.get());
            checkForChange(newNodeList);
            EventBus.getDefault().post(new NodeListUpdatedEvent(Boolean.TRUE));
            Log.i(this.getClass().getSimpleName(), "Node list reloaded");
        } else {
            EventBus.getDefault().post(new NodeListUpdatedEvent(Boolean.FALSE));
            Log.w(this.getClass().getSimpleName(), "Node list reload failed. Keeping old data");
        }
    }

    public Optional<Node> getGatewayByName(final String name) {
        return Iterables.tryFind(fetchGWList(), new Predicate<Node>() {
            @Override
            public boolean apply(Node input) {
                return input.getName().contains(name.substring(0, 5));
            }
        });
    }

    public Optional<NodeDetail> getDetailNodeById(String id) {
        final Gson gson = new Gson();
        Optional<NodeDetail> nodeDetailOptional = Optional.absent();
        final Type nodeDetailType = new TypeToken<NodeDetail>() {
        }.getType();

        try {
            JsonReader reader = getDetailNodeList();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if ("nodes".equals(name)) {
                    reader.beginObject();
                } else if (name.equals(id)) {

                    nodeDetailOptional = Optional.of((NodeDetail) gson.fromJson(reader, nodeDetailType));
                    break;
                } else {
                    reader.skipValue();
                }
            }
            reader.close();
            Log.d(this.getClass().getSimpleName(), "Reading node detail list successful");
        } catch (IOException e) {
            Log.w(this.getClass().getSimpleName(), "Unable to read node detail list", e);
        }
        return nodeDetailOptional;
    }

    private void checkForChange(List<Node> newNodeListImut) {
        if (preferenceController.isNodeMonitoringEnabled()) {
            final List<Node> observedNodeList = preferenceController.getObservedNodeList();
            final List<Node> newNodeList = Lists.newArrayList(newNodeListImut);

            Log.d(this.getClass().getSimpleName(), "Determining node status of observed nodes");
            for (final Node observedNode : observedNodeList) {
                Optional<Node> newNodeOptional = Iterables.tryFind(newNodeList, new Predicate<Node>() {
                    @Override
                    public boolean apply(Node input) {
                        return input.equals(observedNode);
                    }
                });
                if (newNodeOptional.isPresent()) {
                    determineStatus(observedNode, newNodeOptional.get());
                } else {
                    Log.w(this.getClass().getSimpleName(), "Observed node " + observedNode.getName() + " could not be found new list. Preserving old state.");
                }
            }
        } else {
            Log.i(this.getClass().getSimpleName(), "Node monitoring disabled");
        }
    }

    private void determineStatus(Node observedNode, Node newNode) {
        if (newNode != null && newNode.getStatus().getOnline() != observedNode.getStatus().getOnline()) {
            preferenceController.addNodeToObservedList(newNode);
            EventBus.getDefault().post(new NodeStatusChangedEvent(newNode));
        }
    }

    private List<Node> loadList() {
        final FreifunkRestConsumer freifunkService;
        List<Node> nodeList = Collections.emptyList();
        try {
            freifunkService = retrofitServiceManager.getFreifunkService();
            Call<NodeList> call = freifunkService.getNodeList();
            Response<NodeList> response = call.execute();
            if (response.isSuccessful()) {
                nodeList = response.body().getNodes();
                Log.d(this.getClass().getSimpleName(), "Checked for new node list from server");
            } else {
                Log.w(this.getClass().getSimpleName(), "Response no success, error code: " + response.code());
            }
        } catch (IOException e) {
            Log.w(this.getClass().getSimpleName(), "Unable to fetch NodeList", e);
        }
        return nodeList;
    }

    private JsonReader getDetailNodeList() {
        long currentTime = new Date().getTime();
        long lastUpdate = preferenceController.getLastNodeDetailUpdate();
        long updateInterval = preferenceController.getAlarmInterval();
        if (currentTime - lastUpdate > updateInterval) {
            reloadDetailNodeList();
        }
        String content = preferenceController.getNodeDetailString();
        return new JsonReader(new InputStreamReader(new ByteArrayInputStream(content.getBytes(Charsets.UTF_8))));
    }

    private void reloadDetailNodeList() {
        try {
            final FreifunkRestConsumer freifunkService = retrofitServiceManager.getFreifunkService();
            Call<ResponseBody> call = freifunkService.getNodeDetailList();
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()) {
                preferenceController.saveNodeDetailInputStream(response.body().byteStream());
                Log.d(this.getClass().getSimpleName(), "Reloaded nodes.json from Server");
            } else {
                Log.w(this.getClass().getSimpleName(), "Response no success, error code: " + response.code());
            }
        } catch (IOException e) {
            Log.w(this.getClass().getSimpleName(), "Unable to fetch NodeDetailList", e);
        }
    }

    /**
     * Method to remove VPNs from nodeList by regular expression "vpn\d\d"
     */
    private List<Node> justVPNs(List<Node> nodes){
        List<Node> filterNodes = new ArrayList<Node>();
        for (Node node: nodes){
            if (node.getName().matches("vpn\\d\\d")) {
                filterNodes.add(node);
            }
        }
        return filterNodes;
    }

    private List<Node> justNodes(List<Node> nodes){
        List<Node> filterNodes = new ArrayList<Node>();
        for (Node node: nodes){
            if (!node.getName().matches("vpn\\d\\d")) {
                filterNodes.add(node);
            }
        }
        return filterNodes;
    }
}