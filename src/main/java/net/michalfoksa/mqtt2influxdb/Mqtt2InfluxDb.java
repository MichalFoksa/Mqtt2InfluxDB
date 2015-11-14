package net.michalfoksa.mqtt2influxdb;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.michalfoksa.mqtt2influxdb.dao.Destination;
import net.michalfoksa.mqtt2influxdb.rule.FilterRule;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mqtt2InfluxDb {

    static Logger log = LoggerFactory.getLogger(Mqtt2InfluxDb.class) ;

    private BrokerDescriptor brokerDescriptor;
    private MqttCallback callback;
    private Map<String, Integer> subscribedTopicNames;

    private List<Destination> destinations;

    MqttClient mqttClient = null;
    MqttConnectOptions options = null;

    @ConstructorProperties({ "brokerDescriptor", "destination", "filterRules" })
    public Mqtt2InfluxDb(BrokerDescriptor brokerDescriptor, Destination destination,
            List<FilterRule> filterRules) {

        super();

        this.brokerDescriptor = brokerDescriptor;
        callback = new Callback( filterRules );
        destinations = new ArrayList<Destination>();
        destinations.add(destination);
    }

    @ConstructorProperties({ "brokerDescriptor" , "destinations" , "filterRules" })
    public Mqtt2InfluxDb(BrokerDescriptor brokerDescriptor,
            List<Destination> destinations, List<FilterRule> filterRules) {

        super();

        this.brokerDescriptor = brokerDescriptor;
        callback = new Callback(filterRules);
        this.destinations = destinations;
    }

    public void start() throws MqttException {

        do {
            // Connected each disconnected destination
            for ( Destination destination : destinations ) {
                if ( !destination.isConnected() ){
                    destination.connect();
                }
            }


            if (mqttClient == null){
                mqttClient = getMqttClient(brokerDescriptor);
                mqttClient.setCallback( callback );

                options = getMqttConnectOptions(brokerDescriptor);
            }


            // Connect broker when disconnected
            if ( !mqttClient.isConnected() ) {

                try {

                    log.info("Connecting MQTT broker {}" , mqttClient.getServerURI());
                    mqttClient.connect(options);
                    log.info("MQTT broker connected to {}" , mqttClient.getServerURI());

                    if ( subscribedTopicNames == null || subscribedTopicNames.size() == 0) {
                        log.warn("No subscriptions set! Subscribing to topic #, QOS 0");
                        mqttClient.subscribe("#", 0);
                    } else {
                        // Subscribe to each topic name
                        for ( String topicName : subscribedTopicNames.keySet() ){
                            int qos;
                            if ( subscribedTopicNames.get(topicName) == null ){
                                qos = 1;
                            } else {
                                qos = subscribedTopicNames.get(topicName).intValue();
                            }
                            log.debug("Subscribing to topic name \"{}\", QOS {}." , topicName , qos);
                            mqttClient.subscribe(topicName, qos);
                        }
                        log.debug("All topics subscribed.");
                    }

                } catch (MqttException e) {
                    log.error("Connecting MQTT broker failed: " + e.getMessage());
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e1) {
                        log.error("Received InterruptedException while reconnecting broker: {}" , e.getMessage() );
                    }
                }
            } // if mqtt not connected

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                log.error("Received InterruptedException: {}" , e.getMessage() );
            }
        } while (true) ;

    }

    protected MqttClient getMqttClient (BrokerDescriptor brokerDescriptor) throws MqttException{

        String clientId = brokerDescriptor.getClientId();
        if ( clientId == null ){
            clientId = MqttClient.generateClientId();
        }

        MqttClient client = new MqttClient(
                brokerDescriptor.getUris().get(0),  // URI
                clientId,                           //ClientId
                new MemoryPersistence());           //Persistence

        return client ;
    }

    protected MqttConnectOptions getMqttConnectOptions (BrokerDescriptor brokerDescriptor){

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(brokerDescriptor.getMqttVersion());
        options.setServerURIs(brokerDescriptor.getUris().toArray(new String[0]));

        return options ;
    }

    public void addDestinations(List<Destination> destinations) {
        this.destinations.addAll(destinations);
    }

    public void setSubscribedTopicNames(Map<String, Integer> subscribedTopicNames) {
        this.subscribedTopicNames = subscribedTopicNames;
    }

    public void addSubscribedTopicName(String topicName, int qos) {
        if (subscribedTopicNames == null){
            subscribedTopicNames = new TreeMap<String, Integer>();
        }
        subscribedTopicNames.put(topicName, qos);
    }
}
