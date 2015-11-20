package net.michalfoksa.mqtt2influxdb.dao;

import java.beans.ConstructorProperties;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Pong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

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
        Preconditions.checkNotNull(influxDBclient, "InfluxDb " + uri + " is not yet connected");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(databaseName), "Database name must not be null or empty.");

        org.influxdb.dto.Point influxPoint = toInfluxDbPoint(point);

        for ( int i = 0 ; i < retryAttempts ; i++ ){
            try{
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

    /***
     * Converts net.michalfoksa.mqtt2influxdb.dto.Point to org.influxdb.dto.Point
     * @param point
     *          to be converted
     * @return point
     */
    protected org.influxdb.dto.Point toInfluxDbPoint( Point point ){
        return org.influxdb.dto.Point
                .measurement(point.getMeasurement())
                .fields(point.getFields())
                .tag(point.getTags())
                .time(point.getTime() , point.getPrecision())
                .build();
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
