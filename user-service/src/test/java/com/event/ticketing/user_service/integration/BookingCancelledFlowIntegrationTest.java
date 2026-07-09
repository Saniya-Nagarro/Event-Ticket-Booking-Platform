package com.event.ticketing.user_service.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookingCancelledFlowIntegrationTest {

	private static String customerToken;
	private static String adminToken;
	private static Long eventId;
	private static Long bookingId;

	@BeforeAll
	static void setup() {
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
	}

	@Test
	@Order(1)
	void loginCustomer() {
		customerToken = given().baseUri("http://localhost:7071").contentType(ContentType.JSON).body("""
				{
				  "email": "testcustomer@gmail.com",
				  "password": "Password@123"
				}
				""").when().post("/api/auth/login").then().statusCode(200).body("token", notNullValue()).extract()
				.path("token");
	}

	@Test
	@Order(2)
	void loginAdmin() {
		adminToken = given().baseUri("http://localhost:7071").contentType(ContentType.JSON).body("""
				{
				  "email": "superadmin@gmail.com",
				  "password": "superadmin123"
				}
				""").when().post("/api/auth/login").then().statusCode(200).body("token", notNullValue()).extract()
				.path("token");
	}

	@Test
	@Order(3)
	void createEvent() {
		Integer id = given().baseUri("http://localhost:7072").contentType(ContentType.JSON)
				.header("Authorization", "Bearer " + adminToken).body("""
						{
						  "name": "Integration Test Event",
						  "description": "Created from integration test",
						  "venue": "Delhi",
						  "eventDateTime": "2026-08-20T18:00:00",
						  "totalSeats": 10,
						  "ticketPrice": 1000
						}
						""").when().post("/api/events").then().statusCode(anyOf(is(200), is(201)))
				.body("id", notNullValue()).extract().path("id");

		eventId = id.longValue();
	}

	@Test
	@Order(5)
	void publishEvent() {
		given().baseUri("http://localhost:7072").header("Authorization", "Bearer " + adminToken).when()
				.put("/api/events/" + eventId + "/publish").then().statusCode(anyOf(is(200), is(204)));
		System.out.println("Event ID: " + eventId);
	}

	@Test
	@Order(6)
	void bookTicketAndPublishKafkaEvent() {
		Integer id = given().baseUri("http://localhost:7073").contentType(ContentType.JSON)
				.header("Authorization", "Bearer " + customerToken).body("""
						{
						  "eventId": %d,
						  "numberOfTickets": 2
						}
						""".formatted(eventId)).when().post("/api/bookings").then().statusCode(201)
				.body("id", notNullValue()).body("eventId", equalTo(eventId.intValue()))
				.body("numberOfTickets", equalTo(2)).body("status", equalTo("CONFIRMED")).extract().path("id");

		bookingId = id.longValue();

		System.out.println("Booking created successfully. Booking ID: " + bookingId);
		System.out.println("Kafka event BOOKING_CREATED should be published by booking-service.");
	}

	@Test
	@Order(7)
	void verifyAvailableSeatsReduced() {

		given().baseUri("http://localhost:7072").header("Authorization", "Bearer " + adminToken).when()
				.get("/api/events/" + eventId).then().statusCode(200).body("id", equalTo(eventId.intValue()))
				.body("totalSeats", equalTo(10)).body("availableSeats", equalTo(8))
				.body("status", equalTo("PUBLISHED"));

		System.out.println("Verified available seats reduced from 10 to 8.");
	}

	@Test
	@Order(8)
	void cancelBooking() {
		given().baseUri("http://localhost:7073").header("Authorization", "Bearer " + customerToken).when()
				.put("/api/bookings/" + bookingId + "/cancel").then().statusCode(200)
				.body("id", equalTo(bookingId.intValue())).body("status", equalTo("CANCELLED"));

		System.out.println("Booking cancelled successfully. bookingId = " + bookingId);
		System.out.println("Kafka BOOKING_CANCELLED event should be published.");
	}

	@Test
	@Order(9)
	void verifyAvailableSeatsRestoredAfterCancellation() {

		given().baseUri("http://localhost:7072").header("Authorization", "Bearer " + adminToken).when()
				.get("/api/events/" + eventId).then().statusCode(200).body("id", equalTo(eventId.intValue()))
				.body("totalSeats", equalTo(10)).body("availableSeats", equalTo(10))
				.body("status", equalTo("PUBLISHED"));

		System.out.println("Verified seats restored after booking cancellation.");
	}

	@Test
	@Order(10)
	void verifyNotificationViaConsumerLogs() throws InterruptedException {
		Thread.sleep(5000);

		System.out.println("Verify notification-service console/logs for:");
		System.out.println("Consumed BOOKING_CREATED event bookingId=" + bookingId);
		System.out.println("Consumed BOOKING_CANCELLED event bookingId=" + bookingId);
		System.out.println("Notification sent for booking created and booking cancelled.");
	}
}