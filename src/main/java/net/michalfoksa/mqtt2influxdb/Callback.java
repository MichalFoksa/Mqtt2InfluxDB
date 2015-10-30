package net.michalfoksa.mqtt2influxdb;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.michalfoksa.mqtt2influxdb.dto.Point;
import net.michalfoksa.mqtt2influxdb.rule.FilterRule;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Callback implements MqttCallback {

	static Logger log = LoggerFactory.getLogger(Callback.class) ;

	private List<FilterRule> filterRules = new ArrayList<FilterRule>();

	public Callback(List<FilterRule> filterRules) {
        super();
        this.filterRules = filterRules;
    }

	@Override
    public void connectionLost(Throwable error) {
	    log.error( "Connection to broker lost: {}" , error.getMessage() );
    }

	@Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {
    }

	@Override
    public void messageArrived(String topicName, MqttMessage message) {

    	log.debug("Message received. Topic name: " + topicName);

    	for (FilterRule rule : getRulesIterator(topicName , message)){
    	    Point point = null;
    	    try {
    	        point = rule.getPayloadParser().parse(topicName , message);
            } catch (ParseException e) {
                log.warn("Failed to parser payload: {}" , e.getMessage());
                if (rule.continueOnParseFail()){
                    log.warn("Going to next rule.");
                    continue;
                }
                log.warn("Terminating rule chain processing");
                return;
            }

    	    try {
                rule.getDestination().write( point );
            } catch (RuntimeException e) {
                log.error("Exception occured in database write: {} to {}." , e.getMessage() , rule.getDestination());
                log.error( "Terminating" );
                e.printStackTrace();
                System.exit(1);
            }
    	} // for each rule
    }

	public List<FilterRule> getFilterRules() {
    	return filterRules;
    }

	public void setFilterRules(List<FilterRule> filterRules) {
    	this.filterRules = filterRules;
    }

	public void addFilterRule(FilterRule filterRule) {
    	filterRules.add(filterRule);
    }

	/***
	 * Iterator over a FilterRule list.
	 *
	 * @author Michal Foksa
	 */
	protected class RulesIterator implements Iterator<FilterRule> ,
	    Iterable<FilterRule> {

	    Logger log = LoggerFactory.getLogger(RulesIterator.class) ;

        private String topicName;
        private MqttMessage message;
        private Iterator<FilterRule> iterator;
	    private FilterRule next;

	    protected RulesIterator(Iterator<FilterRule> iterator , String topicName, MqttMessage message){
	        this.topicName = topicName;
	        this.message = message;
	        this.iterator = iterator;
	        next = null;
	    }

	    /***
	     * Returns a true only when:
	     *  <li>1. previous rule allows continue to next rule
	     *  <li>2. it matches() == true
	     *  <li>3. has payloadParser set
	     */
        @Override
        public boolean hasNext() {

            if ( next != null && !next.continueToNextRule() ){
                return false;
            }

            while ( iterator.hasNext() ){
                next = iterator.next();
                log.debug("Processing \"{}\" rule pattern" , next.getPatternDescription());
                if ( next.matches(topicName, message) ){

                    if (next.getPayloadParser() == null && !next.continueToNextRule()){
                        log.debug("Message rejected by the rule");
                        log.debug("Terminating rule chain processing");
                        return false;
                    } else {
                        log.debug("Message accepted by the rule for payload parsing");
                        return true;
                    }
                }
                log.debug("Message doesn't match the rule");
            } // while hasNext()
            return false;
        }

        @Override
        public FilterRule next() {
            return next;
        }

        @Override
        public void remove() {
            throw new RuntimeException("remove() is not supported by " + RulesIterator.class + " iterator.");
        }

        @Override
        public Iterator<FilterRule> iterator() {
            return this;
        }
	} // protected class RulesIterator

	protected RulesIterator getRulesIterator(String topicName, MqttMessage message){
	    return new RulesIterator( filterRules.iterator() , topicName , message);
	}
}
