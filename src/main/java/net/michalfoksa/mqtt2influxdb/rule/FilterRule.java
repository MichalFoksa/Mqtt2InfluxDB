package net.michalfoksa.mqtt2influxdb.rule;

import net.michalfoksa.mqtt2influxdb.dao.Destination;
import net.michalfoksa.mqtt2influxdb.parser.PayloadParser;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * <li>Describes whether a message is further processes.
 *
 * <li>Optionally contains Payload parser. It is called to interpret message
 * payload when rule matches.
 *
 * <li>Contains Destination where data are written when message matches the
 * rule and parsing is successful.
 *
 * @author Michal Foksa
 *
 */
public interface FilterRule {

	public boolean matches (String topicName , MqttMessage message);

    /**
     * After match is successful it controls whether rule processing continues
     * to the next rule.
     *
     * @return true to continue processing to the next rule in chain.<br/>
     *         false to terminate chain processing.
     */
	public boolean continueToNextRule();

    /**
     * After match is successful and payload parsing fails it controls whether
     * rule processing continues to the next rule.
     *
     * @return true to continue processing to the next rule in chain.<br/>
     *         false to terminate chain processing.
     */
	public boolean continueOnParseFail();

	public PayloadParser getPayloadParser() ;

	public String getPatternDescription();

	public Destination getDestination();
}
