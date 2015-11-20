# Mqtt2InfluxDB

## Push MQTT messages into InfluxDB.

Utility subscribes to MQTT broker, filters messages through chain of rules and writes them into InfluxDB.

## Prerequisites

#### To Run
 - Java 1.7+

#### To Build
 - Java 1.7+
 - Maven 3.2+

## Features
 - InfluxDB v0.9 support
 - InfluxDB v0.8 support
 - Eclipse Paho Java Client
 - Logback logging

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

 - [example1_home_automation.xml](https://github.com/MichalFoksa/Mqtt2InfluxDB/blob/master/config/example1_home_automation.xml) –
shows how to log data sent on two hardcoded topics: `pre/home/livingroom/temperature` and `pre/home/livingroom/humidity`.

 - [example2_home_automation.xml](https://github.com/MichalFoksa/Mqtt2InfluxDB/blob/master/config/example2_home_automation.xml) –
more elaborate example. It displays how to log data sent on multiple topics using regular expression to identify them. This way all possible topic names do not have to be known beforehand, they just have to follow certain convention given by regular expression(s).

 - [example3_broker_statistics.xml](https://github.com/MichalFoksa/Mqtt2InfluxDB/blob/master/config/example3_broker_statistics.xml) -
displays one way how to track broker performance by subscribing to various `$SYS/…` topics. In this case RSMB broker was used. Other brokers may use different topic names.

### Starting Mqtt2InfluxDB
    java -Dloader.path="config/" -jar target/Mqtt2InfluxDB-0.0.2-SNAPSHOT.jar [your_configuration.xml]

### Tips and Tricks

 - To test your configuration without writing anything into the database, instead of InfluxDB connector use dummy connector. Replace `InfluxDBv09` bean for `DummyInfluxDBv09` in application context, e.g.:
```XML
from <bean id="InfluxDB" class="net.michalfoksa.mqtt2influxdb.dao.InfluxDBv09">
to   <bean id="InfluxDB" class="net.michalfoksa.mqtt2influxdb.dao.DummyInfluxDBv09">
```
*_Similar dummy connector exists also for InfluxDB v0.8._

 - http://regexpal.com/ is handy online regular expression tester.
 - [How to inversion of control (IoC) and dependency injection (DI) patterns in Spring framework](http://howtodoinjava.com/2013/03/19/inversion-of-control-ioc-and-dependency-injection-di-patterns-in-spring-framework-and-related-interview-questions/).
 - [Spring IoC container](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/beans.html) - comprehensive manual.

## License

Mqtt2InfluxDB is licensed under the [MIT License](http://opensource.org/licenses/MIT).
