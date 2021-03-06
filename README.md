# MobileMeshViewer

MobileMeshViewer ermöglicht die Freifunk Knotenstatistik auf dem Mobiltelefon abzurufen und zu überwachen. 
Die App war ein Projekt für "Mobile Computing" an der Hochschule Bremen. Sie wurde entwickelt von Martin Suckau und Jelto Wodstrcil.
Der MobileMeshViewer bietet:

* Anzeige der detailierten Knotenstatisiken
* Anzeige der detailierten Gatewaystatistiken
* Notifications falls ein bestimmter Knoten oder ein Gateway den Status wechselt
* Anpassbare Notifications
* Links zu Social Media und Mail/IRC

<p align="center">
  <img src="docs/screenshot_drawer.png?raw=true" width="200"/>
  <img src="docs/screenshot_nodes.png?raw=true" width="200"/>
  <img src="docs/screenshot_node_detail.png?raw=true" width="200"/>
  <img src="docs/screenshot_about.png?raw=true" width="200"/>
</p>

## Installation

Zurzeit ist die App nur als [.apk im Repository](./app-debug.apk?raw=true) verfügbar. 
Die App wurde mit SDK-Version 25 compiliert und setzt für die Installation mindestens SDK-Version 14 (Android 4.0) vorraus.
Eine Veröffentlichung im Play Store oder auf den offiziellen Freifunk Bremen Servern ist angedacht. Neuere Studio-Versionen werden die fehlenden MIPS und MIPS64 toolchains anmeckern - mips64el-linux-android-4.9 und mipsel-linux-android-4.9 aus android-ndk-r16b-windows-x86_64.zip(https://developer.android.com/ndk/downloads/) umkopieren in das ndk/toolchain Verzeichnis unterhalb des Android SDK-Folders.

## API

Die von der App angezeigten Statistiken werden über externe JSON-Dateien geladen. 
Daten der Freifunk-Knoten werden über die [nodelist.json](https://downloads.bremen.freifunk.net/data/nodelist.json) und die [nodes.json](https://downloads.bremen.freifunk.net/data/nodes.json) in die App importiert. 
Die Daten über die Freifunk-Gateways stammen zu einem Teil aus der [gatemon-json](https://status.bremen.freifunk.net/data/merged.json) und zum anderen ebenfalls aus der [nodes.json](http://downloads.bremen.freifunk.net/data/nodes.json).
Der Parser arbeitet noch mit Version 1 bei der nodes.json - mit yanic entsprechenden Output erzeugen mit: 
<p align="center">
[[nodes.output.meshviewer]]<br>
enable = true<br>
version    = 1<br>
nodes_path = "/var/www/html/meshviewer/data/nodes_v1.json"<br>
graph_path = "/var/www/html/meshviewer/data/graph_v1.json"<br>
</p>
Oder mit dem script nodesv2tov1.pl(https://github.com/Freifunk-Rhein-Sieg/Ansible-Freifunk-Gateway/blob/master/mapserver_collector/files/nodesv2tov1.pl) kompatible nodes_v1.json erzeugen.<br><br>

Freifunk Bremen verwendet FastD - für tunneldigger in den Sourcen das fastd-Parsing auskommentieren.
Die Anzahl der IP-Adressen je Node im nodes.json checken und im Code anpassen.
Bei Freifunk Rhein-Sieg e.V. laufen multiple Domains über die Gateways - Die jeweiligen nodes_v1.json werden über merge_json.pl(https://github.com/Freifunk-Rhein-Sieg/Ansible-Freifunk-Gateway/blob/master/mapserver_collector/files/merge-json.pl) zusammengefahren und für das Ergebnis wird mit generate_nodelist.pl(https://github.com/Freifunk-Rhein-Sieg/Ansible-Freifunk-Gateway/blob/master/mapserver_collector/files/generate_nodelist.pl) eine passende nodelist.json generiert.

## Knotenliste
Die Knotenliste ist die Einstiegsseite der App. Im [Knoten-Fragment](https://github.com/He1md4ll/MobileMeshViewer/blob/master/app/src/main/java/freifunk/bremen/de/mobilemeshviewer/node/NodeListFragment.java) werden alle Freifunk-Knoten angezeigt. Über swipes lässt sich die Liste manuell aktualisieren. Sonst findet die Aktualiserung im Hintergrund statt. Über die Lupe kann die Liste nach bestimmten Knoten-Namen durchsucht werden. Sobald ein Knoten in der Listenansicht ausgewählt wird, öffnet sich dessen Detailansicht.

<p align="center">
  <img src="docs/screenshot_nodes.png?raw=true" width="200"/>
</p>

## Knotendetails
Die Detailansicht präsentiert nahezu alle ferfügbaren Statistiken, die in der [nodes.json](http://downloads.bremen.freifunk.net/data/nodes.json) enthalten sind. Das sind z.B. die MAC- und IP-Adressen oder der hinterlegte Kontakt. Der Status des einzelnen Knotens wird durch die Farbe des Knotennamens in der Toolbar repräsentiert. Rot steht hier für Offline und Grün für Online.

Über den "+"-Button lässt sich ein Knoten markieren. Alle markierten Knoten werden auf Konnektivität überwacht. Die Liste der markierten Knoten kann über den Menüpunkt "Meine Knoten" erreicht werden.

<p align="center">
  <img src="docs/screenshot_node_detail.png?raw=true" width="200"/>
</p>

## Knoten Überwachen
Über die Einstellungen (Aktivitiy [hier](https://github.com/He1md4ll/MobileMeshViewer/blob/master/app/src/main/java/freifunk/bremen/de/mobilemeshviewer/SettingsActivity.java)) lässt sich die Überwachung anpassen. Hier kann der Aktualisierungs-Intervall und die Überwachnungsart konfiguriert werden. Ebenso können Einstellungen für Notifications und Autostart der App getroffen werden.

<p align="center">
  <img src="docs/screenshot_settings.png?raw=true" width="200"/>
</p>

## Gatewayliste
Die Gateway-Liste stellt analog der Knotenliste alle verfügbaren Gateways dar. Sobald ein Gateway aus der Liste angewählt wird, öffnen sich die detailierten Informationen. Die Liste ist ebenfalls mit einem [Fragment](https://github.com/He1md4ll/MobileMeshViewer/blob/master/app/src/main/java/freifunk/bremen/de/mobilemeshviewer/gateway/GatewayListFragment.java) umgesetzt.
<p align="center">
  <img src="docs/screenshot_gateways.png?raw=true" width="200"/>
</p>

## Gateway-Details
Die Detailansicht bietet sowohl die Statistiken, als auch die Test der Gatemon-Test-Knoten. Diese prüfen verteilt die Verfügbarkeit einzelner Dienste.

<p align="center">
  <img src="docs/screenshot_gateway_detail.png?raw=true" width="200"/>
</p>

## Frameworks und technisches
In der App wurden die folgenden Frameworks verwendet:
* Roboguice - Dependency Injection für Android
* Robolectric - Unit Tests mit Android-Aphängigkeiten
* jUnit - Unit Testing Framework
* Mockito - Mocking Framework
* AssertJ - Vereinfachung von Asserts
* Retrofit - REST Client
* Greenrobot - EventBus für Android

Vereinfachtes Klassendiagramm:
<p align="center">
  <img src="docs/uml_klassendiagramm.png?raw=true"/>
</p>

## Zukunft
* Portierung/Generalisierung der App auf andere Communities (z.B. Hamburg, Oldenburg usw.)
* Funktionen Node-Finder
* Netzabdeckung messen
* Öffentliches Git-Repository
