package net.michalfoksa.mqtt2influxdb.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import net.michalfoksa.mqtt2influxdb.dto.Point;

import org.junit.Before;
import org.junit.Test;


public class ToInfluxDbPointTest {

    private Point mfP;

    @Before
    public void initialize() {
        mfP = Point.measurement("measurement")
                .tag( "tag1" , "tag1_val")
                .tag( "tag2" , "tag2_val")
                .field("temperature", 25.1)
                .field("humidity", 80)
                .build();
    }

    @Test
    public void measurementTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{

        org.influxdb.dto.Point infP = InfluxDBv09.toInfluxDbPoint(mfP);

        Field f ;

        f = infP.getClass().getDeclaredField("measurement");
        f.setAccessible(true);
        assertEquals(mfP.getMeasurement(), f.get(infP));
    }

    @Test
    public void tagsTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{

        org.influxdb.dto.Point infP = InfluxDBv09.toInfluxDbPoint(mfP);

        Field f ;

        f = infP.getClass().getDeclaredField("tags");
        f.setAccessible(true);

        assertEquals(mfP.getTags(), f.get(infP));

        mfP = Point.measurement("measurement")
                .tag( "t1" ,"v1" )
                .tag( "t2" ,"v2" )
                .tag( "t3" ,"v3" )
                .field("temperature", 1025)
                .build();

        infP = InfluxDBv09.toInfluxDbPoint(mfP);

        assertEquals(mfP.getTags(), f.get(infP));

        mfP = Point.measurement("measurement")
                .tag( "nonsense" ,"v1" )
                .field("temperature", 1025)
                .build();

        assertFalse(mfP.getTags().equals(f.get(infP)));
    }

    @Test
    public void timeAndPrecisionTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{

        org.influxdb.dto.Point infP = InfluxDBv09.toInfluxDbPoint(mfP);

        Field f ;

        f = infP.getClass().getDeclaredField("time");
        f.setAccessible(true);
        assertEquals(mfP.getTime(), f.get(infP));

        f = infP.getClass().getDeclaredField("precision");
        f.setAccessible(true);
        assertEquals(mfP.getPrecision(), f.get(infP));

        mfP = Point.measurement("measurement")
                .time(123456, TimeUnit.SECONDS)
                .field("temperature", 25.1)
                .build();

        infP = InfluxDBv09.toInfluxDbPoint(mfP);

        f = infP.getClass().getDeclaredField("time");
        f.setAccessible(true);
        assertEquals(123456L, f.get(infP));

        f = infP.getClass().getDeclaredField("precision");
        f.setAccessible(true);
        assertEquals(TimeUnit.SECONDS, f.get(infP));
    }

    @Test
    public void fieldsTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{

        org.influxdb.dto.Point infP = InfluxDBv09.toInfluxDbPoint(mfP);

        Field f ;

        f = infP.getClass().getDeclaredField("fields");
        f.setAccessible(true);

        assertEquals(mfP.getFields(), f.get(infP));

        mfP = Point.measurement("measurement")
                .field("pressure", 1025)
                .field("temperature", 36)
                .field("utilization", 74)
                .build();

        infP = InfluxDBv09.toInfluxDbPoint(mfP);

        assertEquals(mfP.getFields(), f.get(infP));

        mfP = Point.measurement("measurement")
                .field("nonsene", 1025)
                .build();

        assertFalse(mfP.getFields().equals(f.get(infP)));
    }

}
