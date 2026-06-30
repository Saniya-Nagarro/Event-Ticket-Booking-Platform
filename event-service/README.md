# Event Service - Event Ticket Booking Platform

This is the Event Service for a cloud-native Event Ticket Booking Platform.

## Features

- Admin can create, update, publish, and cancel events.
- Customers can view published events.
- Customers can search events by name or venue.
- Customers can check seat availability.
- Booking Service can reduce or increase available seats.
- JWT validation is included.
- Role-based access control is included.

## Tech Stack

- Java 17
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- Spring Security
- PostgreSQL
- JWT
- Lombok
- Maven

## Database

Create PostgreSQL database:

```sql
CREATE DATABASE event_service_db;
```

Update credentials in:

```text
src/main/resources/application.yml
```

## Important JWT Note

The JWT secret in Event Service must match the JWT secret used in User Service.

```yaml
jwt:
  secret: my-secret-key-my-secret-key-my-secret-key
```

## Run

```bash
mvn spring-boot:run
```

Service runs on:

```text
http://localhost:8082
```

## APIs

### Create Event - ADMIN only

```http
POST /api/events
Authorization: Bearer <ADMIN_TOKEN>
```

```json
{
  "name": "Java Tech Conference",
  "description": "Spring Boot and Microservices event",
  "venue": "Delhi Convention Center",
  "eventDateTime": "2026-07-20T10:00:00",
  "totalSeats": 100,
  "ticketPrice": 999.00
}
```

### Update Event - ADMIN only

```http
PUT /api/events/{id}
Authorization: Bearer <ADMIN_TOKEN>
```

```json
{
  "name": "Updated Java Conference",
  "venue": "Noida Convention Center",
  "totalSeats": 120
}
```

### Publish Event - ADMIN only

```http
PUT /api/events/{id}/publish
Authorization: Bearer <ADMIN_TOKEN>
```

### Cancel Event - ADMIN only

```http
PUT /api/events/{id}/cancel
Authorization: Bearer <ADMIN_TOKEN>
```

### Get Published Events - Public

```http
GET /api/events
```

### Get Event Details - Public

```http
GET /api/events/{id}
```

### Search Events - Public

```http
GET /api/events/search?keyword=java
```

### Check Availability - Public

```http
GET /api/events/{id}/availability
```

### Reduce Seats - Authenticated/Internal

```http
PUT /api/events/{id}/reduce-seats?count=2
Authorization: Bearer <TOKEN>
```

### Increase Seats - Authenticated/Internal

```http
PUT /api/events/{id}/increase-seats?count=2
Authorization: Bearer <TOKEN>
```

## RBAC Rules

| API | Access |
|---|---|
| POST /api/events | ADMIN |
| PUT /api/events/{id} | ADMIN |
| PUT /api/events/{id}/publish | ADMIN |
| PUT /api/events/{id}/cancel | ADMIN |
| GET /api/events | Public |
| GET /api/events/{id} | Public |
| GET /api/events/search | Public |
| GET /api/events/{id}/availability | Public |
| PUT /api/events/{id}/reduce-seats | Authenticated |
| PUT /api/events/{id}/increase-seats | Authenticated |

## Testing Flow

1. Start User Service on port 8081.
2. Login as admin using User Service.
3. Copy the admin JWT token.
4. Start Event Service on port 8082.
5. Use admin token to create/publish/cancel events.
