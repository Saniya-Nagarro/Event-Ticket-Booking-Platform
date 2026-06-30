FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/event-service-0.0.1-SNAPSHOT.jar event.jar

EXPOSE 7072

ENTRYPOINT ["java","-jar","event.jar"]