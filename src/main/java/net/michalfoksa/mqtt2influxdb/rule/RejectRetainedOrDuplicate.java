package net.michalfoksa.mqtt2influxdb.rule;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/***
 * Matches any message where duplicate or retained flag is set.<br>
 *
 * If match is true, message processing is aborted.
 *
 * @author Michal Foksa
 *
 */
public class RejectRetainedOrDuplicate extends RejectRule {

	@Override
    public boolean matches(String topicName, MqttMessage message) {
    	return (message.isRetained() || message.isDuplicate());
    }

	@Override
    public String getPatternDescription() {
    	return "Reject retained or duplicate";
    }

}
