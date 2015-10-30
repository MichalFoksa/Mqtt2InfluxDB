package net.michalfoksa.mqtt2influxdb.rule;

import net.michalfoksa.mqtt2influxdb.dao.Destination;
import net.michalfoksa.mqtt2influxdb.parser.PayloadParser;

/**
 * Abstract base class for any rule which purpose is to abort rule processing
 * loop when the rule matches.
 * 
 * @author Michal Foksa
 */
public abstract class RejectRule implements FilterRule {

    public RejectRule() {
        super();
    }

    public boolean continueToNextRule() {
        return false;
    }

    public boolean continueOnParseFail() {
        return false;
    }

    public PayloadParser getPayloadParser() {
        return null;
    }

    public Destination getDestination() {
        return null;
    }

}