FROM openjdk:8
VOLUME /tmp
EXPOSE 10001
ADD ./target/service-iot-0.0.1-SNAPSHOT.jar service-iot.jar
ENTRYPOINT ["java","-jar", "/service-iot.jar"]