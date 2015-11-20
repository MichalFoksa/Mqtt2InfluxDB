package net.michalfoksa.mqtt2influxdb.dao;

import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.impl.InfluxDBImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyInfluxDBv09 extends InfluxDBv09 {

    public static final String MOCK_VERSION = "mock_0.9.x";

    private static Logger log = LoggerFactory.getLogger(DummyInfluxDBv09.class) ;

    public DummyInfluxDBv09(String uri, String username, String password) {
        super(uri, username, password);
    }

    @Override
    protected InfluxDB getInfluxDBClient(String url, String username,
            String password) {
        return new InfluxDBImpl(url, username, password) {

            @Override
            public Pong ping() {
                log.warn("This is dummy implementation. Connection to database IS NOT really opened.");

                Pong pong = new Pong();
                pong.setVersion(MOCK_VERSION);
                return pong;
            }

            @Override
            public void write(String database, String retentionPolicy,
                    Point point) {
                log.warn("This is dummy implementation. NO insert into database is really performed.");
                // Do nothing
                //super.write(database, retentionPolicy, point);
            }

            @Override
            public void write(BatchPoints batchPoints) {
                log.warn("This is dummy implementation. NO insert into database is really performed.");
                // Do nothing
                //super.write(batchPoints);
            }

        };
    };
}
