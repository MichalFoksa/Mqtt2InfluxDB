package net.michalfoksa.mqtt2influxdb.dao;

import java.beans.ConstructorProperties;
import java.util.concurrent.TimeUnit;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.influxdb.v08.InfluxDB;
import org.influxdb.v08.InfluxDB.LogLevel;
import org.influxdb.v08.InfluxDBFactory;
import org.influxdb.v08.dto.Pong;
import org.influxdb.v08.dto.Serie;
import org.influxdb.v08.dto.Serie.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxDBv08 implements Destination {

    static Logger log = LoggerFactory.getLogger(InfluxDBv08.class) ;

    private String uri;
    private String username;
    private String password;
    private String defaultDatabaseName;
    private InfluxDB influxDBclient;

    // Number of attempts to write into the database
    private int retryAttempts;

    @ConstructorProperties({"uri" , "username" , "password"})
    public InfluxDBv08(String uri, String username, String password) {
        super();
        this.uri = uri;
        this.username = username;
        this.password = password;
        retryAttempts = 3;
    }

    @Override
    public void connect() {

        if (influxDBclient == null){
            influxDBclient = getInfluxDBClient(uri, username, password);

            boolean influxDBstarted = false;
            log.info("Connecting to InfluxDB v0.8.x {}" , uri);
            do {
                Pong response;
                response = influxDBclient.ping();
                log.debug("Ping response: {}" , response.toString());
                if (response.getStatus().equalsIgnoreCase("ok")) {
                    influxDBstarted = true;
                } else {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {;}
                    log.debug("Connecting failed. Retrying.");
                }
            } while (!influxDBstarted);
            influxDBclient.setLogLevel(LogLevel.NONE);
        }
        log.info("InfluxDB connected.");
    }

    /**
     * Determines if this database is currently connected to the server.
     *
     * @return <code>true</code> if connected, <code>false</code> otherwise.
     */
    @Override
    public boolean isConnected(){
        if ( influxDBclient != null ){
            return true;
        }
        return false;
    }

    @Override
    public void write(Point point) {
        this.write(point , defaultDatabaseName);
    }

    @Override
    public void write(Point point , String databaseName) {

        if (influxDBclient == null){
            throw new RuntimeException( "InfluxDb " + uri + " is not yet connected");
        }

        Builder serieBuilder = new Serie.Builder(point.getMeasurement());
        serieBuilder.columns( point.getFields().keySet().toArray(new String[0]) );
        serieBuilder.values(point.getFields().values().toArray(new Object[0]) );
        Serie serie = serieBuilder.build();

        for ( int i = 0 ; i < retryAttempts ; i++ ){

            try{
                influxDBclient.write(databaseName , TimeUnit.MILLISECONDS , serie);
                log.debug("Data written into {}." , databaseName);
                break;
            } catch (retrofit.RetrofitError e) {
                log.warn("An error occured in InxluxDB Write: {}" , e.getMessage());
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e1) {;}
                log.debug("Retrying InxluxDB Write");
            } // try

        } // retry
    }

    protected InfluxDB getInfluxDBClient(final String url, final String username,
            final String password) {
        return InfluxDBFactory.connect(uri, this.username, this.password);
    }

    public String getDefaultDatabaseName() {
        return defaultDatabaseName;
    }

    public void setDefaultDatabaseName(String databaseName) {
        defaultDatabaseName = databaseName;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
}
