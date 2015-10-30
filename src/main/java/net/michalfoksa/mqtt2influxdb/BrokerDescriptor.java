package net.michalfoksa.mqtt2influxdb;

import java.beans.ConstructorProperties;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

public class BrokerDescriptor {

	private String uri = null;
	private boolean sslConnection = false;
	private String clientId = null;
	private int mqttVersion = MqttConnectOptions.MQTT_VERSION_3_1;

	@ConstructorProperties( {"uri"} )
	public BrokerDescriptor(String uri) {
    	super();
    	this.uri = uri;
    }

	public String getUri() { return uri; }
	public void setUri(String uri) { this.uri = uri; }

	public boolean isSslConnection() { return sslConnection; }
	public void setSslConnection(boolean sslConnection) { this.sslConnection = sslConnection; }

	public String getClientId() { return clientId; }
	public void setClientId(String clientId) { this.clientId = clientId; }

	public int getMqttVersion() { return mqttVersion; }
	public void setMqttVersion(int mqttVersion) { this.mqttVersion = mqttVersion; }

}
