package net.michalfoksa.mqtt2influxdb.rule;

import java.beans.ConstructorProperties;
import java.util.regex.Pattern;

import net.michalfoksa.mqtt2influxdb.dao.Destination;
import net.michalfoksa.mqtt2influxdb.parser.PayloadParser;
import net.michalfoksa.mqtt2influxdb.util.StringUtils;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Rule which matches topic name against wildcard string. Wildcard is an
 * expression where characters with special meaning are:
 *   <li> star (*) - any number of any characters
 *   <li> question mark (?) - exactly one character<br><br>
 *
 * If match is true, message is processed by PayloadParser. Default behavior is:
 *   <li> If parsing is successful further processing is aborted.
 *   <li> If parsing is fails, message processing continues to next rule.
 *
 * @author Michal Foksa
 */
public class AcceptWildcardRule implements FilterRule {

    static Logger log = LoggerFactory.getLogger(AcceptWildcardRule.class) ;

	private Pattern regExp = null ;
	private String wildcardPattern = null ;
	private boolean continueToNextRule = false ;
	private boolean continueOnParseFail = true ;
	private PayloadParser payloadParser = null ;

	/***
	 * Database where when topic matches the rule parser's result is written
	 * into.
	 */
	private Destination destination = null ;

	@ConstructorProperties( {"wildcardPattern" , "payloadParser" , "destination"} )
	public AcceptWildcardRule(String wildcardPattern , PayloadParser payloadParser , Destination destination) {
    	super();
    	this.wildcardPattern = wildcardPattern;

    	log.debug("wildcard patter converted into regular expression: {}" , StringUtils.wildcardToRegex(wildcardPattern));
    	regExp = Pattern.compile( StringUtils.wildcardToRegex(wildcardPattern) );

    	this.payloadParser = payloadParser;
    	this.destination = destination;
    }

    @Override
    public boolean matches(String topicName, MqttMessage message) {
    	return regExp.matcher(topicName).matches();
    }

	@Override
    public boolean continueToNextRule() {
    	return continueToNextRule;
    }

	@Override
    public boolean continueOnParseFail() {
    	return continueOnParseFail;
    }

	@Override
    public PayloadParser getPayloadParser() {
    	return payloadParser;
    }

    @Override
    public String getPatternDescription() {
        return wildcardPattern;
    }

	public void setContinueToNextRule(boolean continueToNextRule) {
    	this.continueToNextRule = continueToNextRule;
    }

	public void setContinueOnParseFail(boolean continueOnParseFail) {
    	this.continueOnParseFail = continueOnParseFail;
    }

    @Override
    public Destination getDestination() {
        return destination;
    }
}
