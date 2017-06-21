## SuperFit final project

## Build
1a. cd fit-beam-processor
1b. mvn.cmd clean package

2a. cd fit-data-gateway
2b. mvn.cmd clean package

3a. cd fit-fake-android
3b. mvn.cmd clean package

4a. download kafka https://www.apache.org/dyn/closer.cgi?path=/kafka/0.10.2.1/kafka_2.12-0.10.2.1.tgz
4b. unpack kafka
4c. edit fit-kafka-stream\x-kafka-kafka.bat and set kafka dir path

## Run
1. fit-kafka-stream\x-kafka-kafka.bat (wait until finish)
2. java -jar fit-data-gateway\target\fit-data-gateway-1.0.jar
3. java -jar fit-fake-android\target\fit-fake-android-1.0-jar-with-dependencies.jar
4. java -jar fit-beam-processor\target\fit-beam-processor-1.0-jar-with-dependencies.jar
5. visit http://localhost:8080/
