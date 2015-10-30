package net.michalfoksa.mqtt2influxdb.parser;

import java.beans.ConstructorProperties;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Two regular expressions are used to determine measurement and field names.
 * One expression is called measurementName, second one is called fieldName.
 * Each expression must contain one “Capturing group” which value is used as
 * name.<br><br>
 * Message payload is interpreted as a float number value e.g.: 123 or 45.2.
 *
 * <blockquote>
 * <h5>Capturing group:</h5>
 * Besides grouping part of a regular expression together, parentheses also
 * create a numbered capturing group. It stores the part of the string matched
 * by the part of the regular expression inside the parentheses.<br>
 * The regex <code>Set(Value)?</code> matches <code>Set</code> or
 * <code>SetValue</code>. In the first case, the first
 * (and only) capturing group remains empty. In the second case, the first
 * capturing group matches <code>Value</code>.<br>
 *
 * Quoted from:
 * <li> http://www.regular-expressions.info/brackets.html
 * <li> http://www.regular-expressions.info/refcapture.html
 * </blockquote>
 *
 * @author Michal Foksa
 *
 */
public class TopicRegExpParser implements PayloadParser {

    static Logger log = LoggerFactory.getLogger(TopicRegExpParser.class) ;

    private Pattern measurementRegExp = null ;
    private Pattern fieldRegExp = null ;

    @ConstructorProperties({"measurementNameRegExp" , "fieldNameRegExp"})
    public TopicRegExpParser(String measurementNameRegExp,
            String fieldNameRegExp) {
        super();
        measurementRegExp = Pattern.compile( measurementNameRegExp );

        fieldRegExp = Pattern.compile( fieldNameRegExp );
    }

    @Override
    public Point parse(String topicName, MqttMessage message) throws ParseException {
        Matcher m;

        String measurement;
        m = measurementRegExp.matcher(topicName) ;
        m.find();
        try {
            measurement = m.group(1);
            measurement = measurement.replaceAll("/|\\s", "_");
        } catch (IllegalStateException e) {
            // When m.find() fails:
            throw new ParseException("Could not parse topic name \"" + topicName +
                    "\" into measurement name. Regural expression doesn't match " +
                    measurementRegExp.pattern(), 0);
        } catch (IndexOutOfBoundsException e) {
            // When capturing group is not defined in regular expression
            throw new ParseException("Could not parse topic name \"" + topicName +
                    "\" into measurement name. Capturing group is missing in regular expression: " +
                    measurementRegExp.pattern(), 0);
        }

        String field ;
        m = fieldRegExp.matcher(topicName);
        m.find();
        try{
            field = m.group(1);
            field = field.replaceAll("/|\\s", "_");
        } catch (IllegalStateException e) {
            // When m.find() fails:
            throw new ParseException("Could not parse topic name \"" + topicName +
                    "\" into field name. Regural expression doesn't match " +
                    fieldRegExp.pattern(), 0);
        } catch (IndexOutOfBoundsException e) {
            // When capturing group is not defined in regular expression
            throw new ParseException("Could not parse topic name \"" + topicName +
                    "\" into filed name. Capturing group is missing in regular expression: " +
                    fieldRegExp.pattern(), 0);
        }
        Float value;
        try {
            // Remove any non digits and dot characters before parse into float.
            value = new Float( new String(message.getPayload()).replaceAll("[^0-9.]", "") );
        } catch (NumberFormatException e) {
            throw new ParseException( e.getClass().getName() + " occured at message payload parsing: " + e.getMessage() , 0 );
        }

        log.debug("Measurement: \"{}\" field name: \"{}\" value: \"{}\"" , measurement , field , value);

        return Point.measurement(measurement).field(field, value).build();
    }

}
