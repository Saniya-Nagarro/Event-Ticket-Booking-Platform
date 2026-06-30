FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/notification.jar notification.jar

EXPOSE 7074

ENTRYPOINT ["java", "-jar", "notification.jar"]
