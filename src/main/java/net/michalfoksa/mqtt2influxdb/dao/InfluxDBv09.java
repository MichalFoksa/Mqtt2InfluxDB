package net.michalfoksa.mqtt2influxdb.dao;

import java.beans.ConstructorProperties;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfluxDBv09 implements Destination {

    public static final String RETENTION_DEFAULT = "default";

    private static Logger log = LoggerFactory.getLogger(InfluxDBv09.class) ;

    private String uri;
    private String username;
    private String password;
    private String defaultDatabaseName;
    private InfluxDB influxDBclient;

    // Number of attempts to write into the database
    private int retryAttempts;

    @ConstructorProperties({"uri" , "username" , "password"})
    public InfluxDBv09(String uri, String username, String password) {
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
            log.info("Connecting to InfluxDB v0.9.x {}" , uri);
            do {
                Pong response;
                response = influxDBclient.ping();
                log.debug("Ping response: {}" , response.toString());
                if (!response.getVersion().equalsIgnoreCase("unknown")) {
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

//        BatchPoints batchPoints = BatchPoints
//                .database(databaseName)
//                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .retentionPolicy("default")
//                .build();

        //Point point1 = Point.measurement("cpu").field("idle", 90L).field("user", 9L).field("system", 1L).build();
        org.influxdb.dto.Point influxPoint = org.influxdb.dto.Point.measurement(point.getMeasurement()).
                fields(point.getFields()).build();

        for ( int i = 0 ; i < retryAttempts ; i++ ){

            try{
//                influxDBclient.write(databaseName , TimeUnit.MILLISECONDS , serie);
                influxDBclient.write(databaseName, RETENTION_DEFAULT, influxPoint);
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
