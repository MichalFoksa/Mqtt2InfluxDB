package net.michalfoksa.mqtt2influxdb.parser;

import java.beans.ConstructorProperties;
import java.text.ParseException;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Measurement name and field name are hard configured. Parser just tries to
 * interpret message payload as a float number value e.g.: 123 or 45.2.
 *
 * @author Michal Foksa
 */
public class LiteralParser implements PayloadParser {

    static Logger log = LoggerFactory.getLogger(LiteralParser.class) ;

    private String measurementName;
    private String fieldName;

    @ConstructorProperties({"measurementName" , "fieldName"})
    public LiteralParser(String measurementName, String fieldName) {
        super();
        this.measurementName = measurementName;
        this.fieldName = fieldName;
    }

    @Override
    public Point parse(String topicName, MqttMessage message)
            throws ParseException {

        Float value;
        try {
            // Remove any non digits and dot characters before parse into float.
            value = new Float( new String(message.getPayload()).replaceAll("[^0-9.]", "") );
        } catch (NumberFormatException e) {
            throw new ParseException( e.getClass().getName() + " occured at message payload parsing: " + e.getMessage() , 0 );
        }

        log.debug("Measurement: \"{}\" field name: \"{}\" value: \"{}\"" , measurementName , fieldName , value);

        return Point.measurement(measurementName).field(fieldName, value).build();
    }
}
