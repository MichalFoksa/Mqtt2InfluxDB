package net.michalfoksa.mqtt2influxdb.dao;

import java.util.concurrent.TimeUnit;

import org.influxdb.v08.InfluxDB;
import org.influxdb.v08.dto.Pong;
import org.influxdb.v08.dto.Serie;
import org.influxdb.v08.impl.InfluxDBImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyInfluxDBv08 extends InfluxDBv08 {

    private static Logger log = LoggerFactory.getLogger(DummyInfluxDBv08.class) ;

    public DummyInfluxDBv08(String uri, String username, String password) {
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
                pong.setStatus("ok");
                return pong;
            }

            @Override
            public void write(String database, TimeUnit precision,
                    Serie... series) {
                log.warn("This is dummy implementation. NO insert into database is really performed.");
                // Do nothing
                //super.write(database, precision, series);
            }

        };
    };
}



