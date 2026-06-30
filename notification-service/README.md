# Notification Service

Kafka consumer microservice for the Event Ticket Booking Platform.

## Port

```text
7074
```

## Kafka

Default bootstrap server:

```text
localhost:9092
```

For Docker Compose, pass:

```text
SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
```

## Consumed Topics

### Booking
- booking-created-events
- booking-cancelled-events
- booking-failed-events

### Event
- event-created-events
- event-published-events
- event-updated-events
- event-cancelled-events

### User
- user-registered-events
- user-role-changed-events

### Payment
- payment-successful-events
- payment-failed-events

## Build

```bash
mvn clean package
```

## Run

```bash
java -jar target/notification.jar
```

## Health

```http
GET http://localhost:7074/api/notifications/health
```
