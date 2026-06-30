FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/user-service-0.0.1-SNAPSHOT.jar user.jar

EXPOSE 7071

ENTRYPOINT ["java","-jar","user.jar"]