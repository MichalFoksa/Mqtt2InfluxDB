package net.michalfoksa.mqtt2influxdb.parser;

import java.text.ParseException;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/***
 * Describes how to interpret a topic name and message payload into a net.michalfoksa.mqtt2influxdb.dto.Point
 *
 * @author Michal Foksa
 *
 */
public interface PayloadParser {

    public Point parse(String topicName , MqttMessage message) throws ParseException;

}
