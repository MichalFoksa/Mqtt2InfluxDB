package net.michalfoksa.mqtt2influxdb.rule;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.michalfoksa.mqtt2influxdb.TestMqttMessage;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.Test;

public class RulesTest {

    @Test
	public void RejectRetainedMessageTest () {
    	MqttMessage message =  new MqttMessage();
    	message.setRetained(true);

    	RejectRetainedOrDuplicate rule = new RejectRetainedOrDuplicate();

    	basicRejectRuleAsserts(rule);

    	assertTrue("Retained message should have been rejected.", rule.matches(null, message));
    }

    @Test
	public void RejectDuplicateMessageTest () throws Exception {
    	TestMqttMessage message =  new TestMqttMessage();
    	message.setDuplicateFlag(true);

    	RejectRetainedOrDuplicate rule = new RejectRetainedOrDuplicate();

    	basicRejectRuleAsserts(rule);

    	assertTrue("Duplicate message should have been rejected.", rule.matches(null, message));
    }

    @Test
	public void RejectRegExpRuleTest(){

    	RejectRegExpRule rule = null ;
    	rule = new RejectRegExpRule("^[a-zA-Z\\/]*\\[ClientId\\][a-zA-Z+-_=\\/]*$") ;

    	basicRejectRuleAsserts(rule);

    	assertTrue(rule.matches("pre/home/[ClientId]/internal_temperature", null));
    	assertTrue(rule.matches("[ClientId]/internal_temperature", null));
    	assertTrue(rule.matches("pre/home/[ClientId]", null));
    	assertTrue(rule.matches("[ClientId]", null));
    	assertFalse(rule.matches("pre/home/ClientId]/internal_temperature" , null));
    	assertFalse(rule.matches("ClientId]/internal_temperature" , null));
    }

	public void basicRejectRuleAsserts (FilterRule rule){
    	assertFalse( "continueToNextRule() must return false in any reject rule." ,
            	rule.continueToNextRule() );

    	assertNull("Does not make sense for getPayloadParser() to return something in reject rule." ,
            	rule.getPayloadParser() );

        assertNull("Does not make sense for getDestination() to return something in reject rule." ,
                rule.getDestination() );
    }

    @Test
	public void AcceptRegExpRuleTest(){

    	AcceptRegExpRule rule = null ;
    	rule = new AcceptRegExpRule( "^pre\\/home\\/[a-zA-Z0-9]+\\/.*$" , null , null);

    	assertTrue(rule.matches("pre/home/bathroom/internal_temperature" , null));
    	assertTrue(rule.matches("pre/home/livingroom/internal_temperature" , null));
    	assertFalse(rule.matches("test" , null));
    	assertFalse(rule.matches("pre/home/[ClientId]/internal_temperature" , null));
    	assertFalse(rule.matches("pre/home/livingroom" , null));

    	rule = new AcceptRegExpRule( "^pre\\/home\\/[a-zA-Z0-9]+\\/?.*$" , null , null);

    	assertTrue(rule.matches("pre/home/bathroom/internal_temperature" , null));
    	assertTrue(rule.matches("pre/home/livingroom/internal_temperature" , null));
    	assertTrue(rule.matches("pre/home/livingroom" , null));
    	assertFalse(rule.matches("test" , null));
    	assertFalse(rule.matches("pre/home/[ClientId]/internal_temperature" , null));

    }
}
