# Mqtt2InfluxDB

## Push MQTT messages into InfluxDB.

Utility subscribes to MQTT broker, filters messages through chain of rules and writes them into InfluxDB.

## Prerequisites

Java 7 Runtime

## Features

- Logback logging

## Limitations
- InfluxDB v0.8.x support only


## Getting Started

### Building

    mvn package

### Configuration

Application consists of java beans which have to be glued together by a Spring XML application context.

Typical configuration consists following beans:
 -	One BrokerDescriptor – describes how to connect to a MQTT broker.
 -	One or more FilterRules. It:
	 - Describes how message matches the rule.
   - Optionally contains Payload parser. It is called to interpret message payload when rule matches.
   - Contains Destination where data are written when message matches the rule and parsing is successful.
 -	One or more PayloadParsers. They describe how to interpret a message.
 -	One or more Destinations. They describe how data are written into data storage.
 -	One Mqtt2InfluxDb which is the application itself.

Default configuration file name is `applicationContext.xml`. Alternatively configuration file name can be provided on command line.

### Example Configurations

Example configurations are in `config/` directory.

 - `example1_home_automation.xml` – shows how to log data sent on two hardcoded topics: `pre/home/livingroom/temperature` and `pre/home/livingroom/humidity`.

 - `example2_home_automation.xml` – this is more elaborate example. It displays how to log data sent on multiple topics using regular expression to identify them. This way all possible topic names do not have to be known beforehand, they just have to follow certain convention given by regular expression(s).

 - `example3_broker_statistics.xml` - it displays one way how to track broker performance by subscribing to various `$SYS/…` topics. In this case RSMB broker was used. Other brokers may use different topic names.

### Starting Mqtt2InfluxDB
    java -Dloader.path="config/" -jar target/Mqtt2InfluxDB-0.0.1-SNAPSHOT.jar [your_configuration.xml]

### Tips and Tricks

 - To test your configuration without writing anything into database, replace InfluxDB connector for a dummy one. In application context replace `InfluxDBv08` bean for `DummyInfluxDBv08`:

```XML
from <bean id="InfluxDB" class="net.michalfoksa.mqtt2influxdb.dao.InfluxDBv08">
to   <bean id="InfluxDB" class="net.michalfoksa.mqtt2influxdb.dao.DummyInfluxDBv08">
```

 - http://regexpal.com/ is handy online regular expression tester.

## License

Mqtt2InfluxDB is licensed under the [MIT License](http://opensource.org/licenses/MIT).
