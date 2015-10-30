package net.michalfoksa.mqtt2influxdb.dao;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Serie;
import org.influxdb.impl.InfluxDBImpl;

public class DummyInfluxDBv08 extends InfluxDBv08 {

    public DummyInfluxDBv08(String uri, String username, String password) {
        super(uri, username, password);
    }

    @Override
    protected InfluxDB getInfluxDBClient(String url, String username,
            String password) {
        return new InfluxDBImpl(url, username, password) {

            @Override
            public Pong ping() {
                Pong pong = new Pong();
                pong.setStatus("ok");
                return pong;
            }

            @Override
            public void write(String database, TimeUnit precision,
                    Serie... series) {
                // Do nothing
                //super.write(database, precision, series);
            }

        };
    };
}



