package com.event.ticketing.user_service.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PreventOverbookingIntegrationTest {

	private static String customerToken;
	private static String adminToken;
	private static Long eventId;

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
	void createEventWithOnlyOneSeat() {
		Integer id = given().baseUri("http://localhost:7072").contentType(ContentType.JSON)
				.header("Authorization", "Bearer " + adminToken).body("""
						{
						  "name": "Overbooking Test Event",
						  "description": "Only one seat available",
						  "venue": "Delhi",
						  "eventDateTime": "2026-08-25T18:00:00",
						  "totalSeats": 1,
						  "ticketPrice": 1000
						}
						""").when().post("/api/events").then().statusCode(anyOf(is(200), is(201)))
				.body("id", notNullValue()).extract().path("id");

		eventId = id.longValue();
	}

	@Test
	@Order(4)
	void publishEvent() {
		given().baseUri("http://localhost:7072").header("Authorization", "Bearer " + adminToken).when()
				.put("/api/events/" + eventId + "/publish").then().statusCode(anyOf(is(200), is(204)));
	}

	@Test
	@Order(5)
	void preventOverbookingWhenTwoUsersBookSameLastSeat() throws Exception {

		ExecutorService executorService = Executors.newFixedThreadPool(2);
		CountDownLatch startSignal = new CountDownLatch(1);

		Callable<Integer> bookingRequest1 = () -> sendBookingRequest(startSignal);
		Callable<Integer> bookingRequest2 = () -> sendBookingRequest(startSignal);

		Future<Integer> response1 = executorService.submit(bookingRequest1);
		Future<Integer> response2 = executorService.submit(bookingRequest2);

		startSignal.countDown();

		int status1 = response1.get();
		int status2 = response2.get();

		executorService.shutdown();

		System.out.println("Request 1 status = " + status1);
		System.out.println("Request 2 status = " + status2);

		Assertions.assertTrue((status1 == 201 && status2 == 400) || (status1 == 400 && status2 == 201),
				"Expected one booking to succeed and one booking to fail");
	}

	@Test
	@Order(6)
	void verifySeatsNeverGoBelowZero() {

		given().baseUri("http://localhost:7072").header("Authorization", "Bearer " + adminToken).when()
				.get("/api/events/" + eventId).then().statusCode(200).body("id", equalTo(eventId.intValue()))
				.body("totalSeats", equalTo(1)).body("availableSeats", equalTo(0));
	}

	private Integer sendBookingRequest(CountDownLatch startSignal) throws Exception {

		startSignal.await();

		return given().baseUri("http://localhost:7073").contentType(ContentType.JSON)
				.header("Authorization", "Bearer " + customerToken).body("""
						{
						  "eventId": %d,
						  "numberOfTickets": 1

						}
						""".formatted(eventId)).when().post("/api/bookings").then().extract().statusCode();
	}
}