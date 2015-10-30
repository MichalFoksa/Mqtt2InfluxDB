package net.michalfoksa.mqtt2influxdb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Helper class to make available method setDuplicate. 
 * Class is meat to be used in JUnits only.
 */
public class TestMqttMessage extends MqttMessage {
	
	public void setDuplicateFlag(boolean dup) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    	
	    Method setDuplicateMethod = MqttMessage.class.getDeclaredMethod("setDuplicate" , boolean.class);
	    setDuplicateMethod.setAccessible(true);
	    setDuplicateMethod.invoke((MqttMessage)this, true);        	
    }
}
