Eureka service discovery sample.

Add dependency:
spring-cloud-starter-netflix-eureka-client

application.yml:
spring:
  application:
    name: event-service
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

Use @LoadBalanced RestTemplate and call:
http://event-service/api/events
