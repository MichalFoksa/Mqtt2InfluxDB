package net.michalfoksa.mqtt2influxdb.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TopicRegExpParserTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void positiveTest() throws ParseException{

        MqttMessage message = new MqttMessage( "25.1".getBytes() );

        String measurementRegExp = "^pre\\/home\\/([a-zA-Z0-9]+)\\/.+$" ;
        String fieldRegExp = "^pre\\/home\\/[a-zA-Z0-9]+\\/(.+)$";

        TopicRegExpParser parser = new TopicRegExpParser(measurementRegExp , fieldRegExp);
        Point point = parser.parse("pre/home/bathroom/temperature" , message);

        assertEquals("bathroom", point.getMeasurement());
        assertFalse(point.getFields().isEmpty());
        assertTrue(point.getFields().containsKey("temperature"));
        assertTrue(point.getFields().get("temperature").equals(new Float(25.1)));

        point = parser.parse("pre/home/livingroom/internal_temperature", message);

        assertEquals("livingroom", point.getMeasurement());
        assertFalse(point.getFields().isEmpty());
        assertTrue(point.getFields().containsKey("internal_temperature"));


        point = parser.parse("pre/home/livingroom/device/battery/volatage", message);

        assertEquals("livingroom", point.getMeasurement());
        assertFalse(point.getFields().isEmpty());
        assertTrue(point.getFields().containsKey("device_battery_volatage"));
    }

    @Test
    public void doesNotMatchTest() throws ParseException{

        TopicRegExpParser parser = new TopicRegExpParser("^match$" , "^match$");

        exception.expect( ParseException.class );
        exception.expectMessage("Could not parse topic name \"does_not_match\" into measurement name. Regural expression doesn't match ^match$");
        parser.parse("does_not_match", null);
    }

    @Test
    public void missingGroupTest() throws ParseException{

        TopicRegExpParser parser = new TopicRegExpParser("^match$" , "^match$");

        exception.expect( ParseException.class );
        exception.expectMessage("Could not parse topic name \"match\" into measurement name. Capturing group is missing in regular expression: ^match$");
        parser.parse("match", null);
    }


}
