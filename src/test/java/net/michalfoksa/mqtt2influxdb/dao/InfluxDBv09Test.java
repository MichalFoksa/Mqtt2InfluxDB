package net.michalfoksa.mqtt2influxdb.dao;

import static org.junit.Assert.assertEquals;
import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Pong;
import org.influxdb.impl.InfluxDBImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class InfluxDBv09Test {

    int retries = 1;
    InfluxDBReconect db ;

    class InfluxDBReconect extends InfluxDBv09 {

        public InfluxDBReconect(String uri, String username, String password) {
            super(uri, username, password);
        }

        int retryCount = 0;

        @Override
        protected InfluxDB getInfluxDBClient(String url, String username, String password) {
            return new InfluxDBImpl(url, username, password){

                @Override
                public Pong ping() {
                    retryCount++;
                    Pong pong = new Pong();
                    if ( retryCount < retries ){
                        pong.setVersion("unknown");
                    } else {
                        pong.setVersion(DummyInfluxDBv09.MOCK_VERSION);
                        assertEquals(retries, retryCount);
                    }
                    return pong;
                }

                @Override
                public void write(String database, String retentionPolicy, org.influxdb.dto.Point point){
                    if (point == null){
                        return;
                    }
                };
            };
        }
    }

    @Before
    public void initialize(){
        db = new InfluxDBReconect("http://localhost:8086" , "user_name" , "password");
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void disconectedWriteTest(){

        exception.expect( RuntimeException.class );
        exception.expectMessage( "InfluxDb null is not yet connected" );
        new InfluxDBv09(null , null , null).write(null);
    }

    @Test
    public void connectTest(){
        retries = 1;
        db.connect();
    }

    @Test
    public void connectTest2(){
        retries = 5;
        db.connect();
    }

    @Test
    public void witeTest(){
        retries = 1;
        db.connect();

        db.write( Point.measurement("measurement")
                .field("temperature", 25.1)
                .field("humidity", 80)
                .build() );
    }
}
