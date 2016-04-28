package freifunk.bremen.de.mobilemeshviewer;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import org.greenrobot.eventbus.EventBus;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

import java.util.List;

import freifunk.bremen.de.mobilemeshviewer.alarm.AlarmController;
import freifunk.bremen.de.mobilemeshviewer.gateway.GatewayListFragment;
import freifunk.bremen.de.mobilemeshviewer.gateway.model.Gateway;
import freifunk.bremen.de.mobilemeshviewer.gateway.model.IpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Can not use Fragment lifecycle directly because it's still buggy in Robolectric
 */
public class CustomListFragmentTest extends RobolectricTest {
    @Mock
    private ListLoader<Gateway> gatewayListLoader;
    @Mock
    private AlarmController alarmController;
    private GatewayListFragment classUnderTest;
    private ActivityController<MeshViewerActivity> controller;

    @Override
    public void setUp() throws Exception {
        classUnderTest = new GatewayListFragment();
        super.setUp();
        controller = Robolectric.buildActivity(MeshViewerActivity.class)
                .create()
                .start()
                .resume()
                .visible();
    }

    @Test
    public void testOnStart() throws Exception {
        // Then
        Mockito.verify(gatewayListLoader).onContentChanged();
        assertThat(EventBus.getDefault().isRegistered(classUnderTest)).isTrue();
    }

    @Test
    public void testOnStop() throws Exception {
        // When
        controller.stop().destroy();

        // Then
        assertThat(EventBus.getDefault().isRegistered(classUnderTest)).isFalse();
    }

    @Test
    public void testOnActivityCreated() throws Exception {
        // Given
        assertThat(classUnderTest.getListView().getAdapter()).isNotNull();
        assertThat(classUnderTest.getListView().isShown()).isFalse();
        assertThat(classUnderTest.getLoaderManager().getLoader(1)).isEqualTo(gatewayListLoader);
        Mockito.verify(gatewayListLoader).reset();
    }

    @Test
    public void testOnLoadFinished() throws Exception {
        // Given
        final Gateway gateway = new Gateway("test", IpStatus.BOTH, IpStatus.BOTH, IpStatus.BOTH, IpStatus.BOTH);
        List<Gateway> list = Lists.newArrayList(gateway);
        final int position = classUnderTest.getListView().getFirstVisiblePosition();

        // When
        classUnderTest.onLoadFinished(gatewayListLoader, list);

        // Then
        assertThat(classUnderTest.getAdapter().getItem(0)).isEqualTo(gateway);
        assertThat(classUnderTest.getListView().getFirstVisiblePosition()).isEqualTo(position);
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        // Given
        final Gateway gateway = new Gateway("test", IpStatus.BOTH, IpStatus.BOTH, IpStatus.BOTH, IpStatus.BOTH);
        List<Gateway> list = Lists.newArrayList(gateway);
        classUnderTest.onLoadFinished(gatewayListLoader, list);

        // When
        classUnderTest.onLoaderReset(gatewayListLoader);

        // Then
        assertThat(classUnderTest.getAdapter().getCount()).isZero();
    }

    @Override
    public AbstractModule getModuleForInjection() {
        return new TestModule();
    }

    private class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(AlarmController.class).toInstance(alarmController);
            bind(GatewayListFragment.class).toInstance(classUnderTest);
        }

        @Provides
        public ListLoader<Gateway> getStuff() {
            return gatewayListLoader;
        }
    }

    // TODO: Test onReloadFinished
}