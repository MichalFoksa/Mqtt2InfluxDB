package net.michalfoksa.mqtt2influxdb.dao;

import net.michalfoksa.mqtt2influxdb.dto.Point;

public interface Destination {

    public void connect();

    /**
     * Determines if this destination is currently connected to the server.
     *
     * @return <code>true</code> if connected, <code>false</code> otherwise.
     */
    public boolean isConnected();

    public void write ( Point point );

    public void write ( Point point , String databaseName);
}
