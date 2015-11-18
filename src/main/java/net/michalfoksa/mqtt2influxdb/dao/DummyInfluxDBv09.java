package net.michalfoksa.mqtt2influxdb.dao;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.impl.InfluxDBImpl;

public class DummyInfluxDBv09 extends InfluxDBv09 {

    public static final String MOCK_VERSION = "mock_0.9.x";

    public DummyInfluxDBv09(String uri, String username, String password) {
        super(uri, username, password);
    }

    @Override
    protected InfluxDB getInfluxDBClient(String url, String username,
            String password) {
        return new InfluxDBImpl(url, username, password) {

            @Override
            public Pong ping() {
                Pong pong = new Pong();
                pong.setVersion(MOCK_VERSION);
                return pong;
            }

            @Override
            public void write(String database, String retentionPolicy,
                    Point point) {
                // Do nothing
                //super.write(database, retentionPolicy, point);
            }

            @Override
            public void write(BatchPoints batchPoints) {
                // Do nothing
                //super.write(batchPoints);
            }

        };
    };
}
