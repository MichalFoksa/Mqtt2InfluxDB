package net.michalfoksa.mqtt2influxdb.rule;

import java.beans.ConstructorProperties;
import java.util.regex.Pattern;

import net.michalfoksa.mqtt2influxdb.dao.Destination;
import net.michalfoksa.mqtt2influxdb.parser.PayloadParser;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public class AcceptRegExpRule implements FilterRule {

	private Pattern regExp = null ;
	private String patternDescription = null ;
	private boolean continueToNextRule = false ;
	private boolean continueOnParseFail = true ;
	private PayloadParser payloadParser = null ;

	/***
	 * Database where when topic matches the rule parser's result is written
	 * into.
	 */
	private Destination destination = null ;

	@ConstructorProperties( {"regExp" , "payloadParser" , "destination"} )
	public AcceptRegExpRule(String regExp , PayloadParser payloadParser , Destination destination) {
    	super();
    	this.regExp = Pattern.compile( regExp );
    	this.payloadParser = payloadParser;
    	this.destination = destination;
    }

	public boolean matches(String topicName, MqttMessage message) {
    	return regExp.matcher(topicName).matches();
    }

	public boolean continueToNextRule() {
    	return continueToNextRule;
    }

	public boolean continueOnParseFail() {
    	return continueOnParseFail;
    }

	public PayloadParser getPayloadParser() {
    	return payloadParser;
    }

	public void setPatternDescription(String patternDescription) {
        this.patternDescription = patternDescription;
    }

    public String getPatternDescription() {
        if ( patternDescription == null ){
            return regExp.pattern();
        }
        return patternDescription;
    }

	public void setContinueToNextRule(boolean continueToNextRule) {
    	this.continueToNextRule = continueToNextRule;
    }

	public void setContinueOnParseFail(boolean continueOnParseFail) {
    	this.continueOnParseFail = continueOnParseFail;
    }

    public Destination getDestination() {
        return destination;
    }
}
