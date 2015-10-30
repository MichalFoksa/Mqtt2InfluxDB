package net.michalfoksa.mqtt2influxdb;

import org.springframework.context.support.FileSystemXmlApplicationContext;

public class Application {

    static final String defaultContextName = "applicationContext.xml";

    @SuppressWarnings("resource")
    public static void main(String[] args) {

        String applicationContextName;
        if (args.length == 1){
            applicationContextName = args[0];
        } else {
            applicationContextName = defaultContextName;
        }

        new FileSystemXmlApplicationContext( applicationContextName );
    }

}
