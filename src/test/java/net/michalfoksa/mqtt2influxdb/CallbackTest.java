package net.michalfoksa.mqtt2influxdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.michalfoksa.mqtt2influxdb.Callback.RulesIterator;
import net.michalfoksa.mqtt2influxdb.parser.TopicRegExpParser;
import net.michalfoksa.mqtt2influxdb.rule.AcceptRegExpRule;
import net.michalfoksa.mqtt2influxdb.rule.RejectRegExpRule;
import net.michalfoksa.mqtt2influxdb.rule.RejectRetainedOrDuplicate;
import net.michalfoksa.mqtt2influxdb.rule.FilterRule;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Before;
import org.junit.Test;

public class CallbackTest {

    List<FilterRule> topicRules;
    TopicRegExpParser topicRegExpParser;
    AcceptRegExpRule acceptRule1; // accept pre/home/*** topics

    Callback callback = new Callback(topicRules){
        @Override
        protected RulesIterator getRulesIterator(String topicName, MqttMessage message) {
            return new RulesIterator(topicRules.iterator(), topicName, message);
        };
    };

    @Before
    public void initializeTopicRules(){

        String measurementRegExp = "^pre\\/home\\/([a-zA-Z0-9]+)\\/.+$" ;
        String fieldRegExp = "^pre\\/home\\/[a-zA-Z0-9]+\\/(.+)$";

        topicRegExpParser = new TopicRegExpParser(measurementRegExp , fieldRegExp);
        acceptRule1 = new AcceptRegExpRule("^pre\\/home\\/[a-zA-Z0-9]+\\/.+$" , topicRegExpParser , null);

        topicRules = new ArrayList<FilterRule>();
        topicRules.add( new RejectRetainedOrDuplicate() );
        // Reject *[ClientId]* topics
        topicRules.add( new RejectRegExpRule("^[a-zA-Z\\/]*\\[ClientId\\][a-zA-Z+-_=\\/]*$") );
        // accept pre/home/*** topics
        topicRules.add( acceptRule1 );
    }

    @Test
    public void rejectRetainedOrDuplicateTest() throws Exception {

        RulesIterator it;

        MqttMessage retainedMessage =  new MqttMessage();
        retainedMessage.setRetained(true);

        it = callback.getRulesIterator(null, retainedMessage);
        assertFalse("Retained message must be rejected" , it.hasNext());

        TestMqttMessage duplicateMessage =  new TestMqttMessage();
        duplicateMessage.setDuplicateFlag(true);

        it = callback.getRulesIterator(null, duplicateMessage);
        assertFalse("Duplicate message must be rejected" , it.hasNext());
    }

    @Test
    public void validTopicNameTest() throws Exception {

        RulesIterator it;

        it = callback.getRulesIterator("pre/home/kitchen/temperature", new MqttMessage());
        assertTrue("pre/home/kitchen/temperature is valid topic name and must exists a matchig rule for it" ,
                it.hasNext());
        assertEquals(acceptRule1, it.next());
        assertEquals(topicRegExpParser, it.next().getPayloadParser());
    }


    @Test
    public void invalidTopicNameTest() throws Exception {

        RulesIterator it;
        String topicName = "pre/home/kitchen" ;
        it = callback.getRulesIterator(topicName , new MqttMessage());
        assertFalse(topicName + " is not not valid topic name" , it.hasNext());

        topicName = "pre/home/kitchen/" ;
        it = callback.getRulesIterator(topicName , new MqttMessage());
        assertFalse(topicName + " is not not valid topic name" , it.hasNext());

        topicName = "/pre/home/kitchen/tempertature" ;
        it = callback.getRulesIterator(topicName , new MqttMessage());
        assertFalse(topicName + " is not not valid topic name" , it.hasNext());
    }

}
