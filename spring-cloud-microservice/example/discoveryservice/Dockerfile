FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/discoveryservice-1.0.jar discovery-service.jar
ENTRYPOINT ["java", "-jar", "discovery-service.jar"]