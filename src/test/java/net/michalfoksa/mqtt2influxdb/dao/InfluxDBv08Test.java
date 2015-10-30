package net.michalfoksa.mqtt2influxdb.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Serie;
import org.influxdb.impl.InfluxDBImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


public class InfluxDBv08Test {

    int retries = 1;
    InfluxDBReconect db ;

    class InfluxDBReconect extends InfluxDBv08 {

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
                        pong.setStatus("nok");
                    } else {
                        pong.setStatus("ok");
                        assertEquals(retries, retryCount);
                    }
                    return pong;
                }

                @Override
                public void write(final String database, final TimeUnit precision, final Serie... series){
                    if (series == null || series.length == 0){
                        return;
                    }

                    assertEquals("measurement" , series[0].getName());

                    assertEquals(1 , series[0].getRows().size() );

                    Map<String, Object> row = series[0].getRows().get(0);
                    assertNotNull(row);

                    assertNotNull(row.get("temperature"));
                    assertEquals( 25.1 , row.get("temperature") );

                    assertNotNull(row.get("humidity"));
                    assertEquals( 80 , row.get("humidity") );

//                    for ( Map<String, Object> r : series[0].getRows() ){
//                        for ( int i = 0 ; i < series[0].getColumns().length ; i++ ){
//                            String columnName = series[0].getColumns()[i];
//                            System.out.println(
//                                    columnName + " " +
//                                        r.get(columnName).toString()
//                                    );
//                        }
//                    }
                }

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
        new InfluxDBv08(null , null , null).write(null);
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
