package net.michalfoksa.mqtt2influxdb.rule;

import java.util.regex.Pattern;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class RejectRegExpRule extends RejectRule {
	
	private Pattern regExp = null ;
	
	public RejectRegExpRule(String regExpString) {
    	super();
    	this.regExp = Pattern.compile( regExpString );
    }

	public boolean matches(String topicName, MqttMessage message) {
    	return this.regExp.matcher(topicName).matches();
    }

	public String getPatternDescription() {
    	return regExp.pattern() ;
    }

}
