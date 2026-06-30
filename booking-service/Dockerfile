FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/booking-service-0.0.1-SNAPSHOT.jar booking.jar

EXPOSE 7073

ENTRYPOINT ["java","-jar","booking.jar"]