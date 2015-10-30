package net.michalfoksa.mqtt2influxdb.rule;

import java.beans.ConstructorProperties;
import java.util.regex.Pattern;

import net.michalfoksa.mqtt2influxdb.util.StringUtils;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Rule which matches topic name against wildcard string. Wildcard is an
 * expression where characters with special meaning are:
 *   <li> star (*) - any number of any characters
 *   <li> question mark (?) - exactly one character
 * <br><br>
 *
 * If match is true, message processing is aborted.
 *
 * @author Michal Foksa
 */
public class RejectWildcardRule extends RejectRule {

    static Logger log = LoggerFactory.getLogger(RejectWildcardRule.class) ;

    private Pattern regExp = null ;
    private String wildcardPattern = null ;

    @ConstructorProperties( {"wildcardPattern"} )
    public RejectWildcardRule( String wildcardPattern ) {
        super();
        this.wildcardPattern = wildcardPattern;

        log.debug("wildcard patter converted into regular expression: {}" , StringUtils.wildcardToRegex(wildcardPattern));
        regExp = Pattern.compile( StringUtils.wildcardToRegex(wildcardPattern) );
    }

    @Override
    public boolean matches(String topicName, MqttMessage message) {
        return regExp.matcher(topicName).matches();
    }

    @Override
    public String getPatternDescription() {
        return wildcardPattern;
    }
}
