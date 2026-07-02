package com.event.ticketing.booking_service.service.impl.test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentBookingTest {

    private static final String API_URL = "http://localhost:7073/api/bookings";

    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0NkBnbWFpbC5jb20iLCJyb2xlIjoiQ1VTVE9NRVIiLCJpYXQiOjE3ODI5NzEzNzEsImV4cCI6MTc4MzA1Nzc3MX0.UbS0c6sYSrrl5YCZDUNHrQgyC0m_9juisMXySgwhZhU";

    public static void main(String[] args) throws Exception {

        HttpClient httpClient = HttpClient.newHttpClient();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        CountDownLatch startSignal = new CountDownLatch(1);

        Runnable request1 = () -> sendBookingRequest(
                httpClient,
                startSignal,
                "Request-1",
                1L,
                1
        );

        Runnable request2 = () -> sendBookingRequest(
                httpClient,
                startSignal,
                "Request-2",
                11L,
                1
        );

        executorService.submit(request1);
        executorService.submit(request2);

        System.out.println("Both requests are ready...");
        System.out.println("Sending both requests now...");

        startSignal.countDown();

        executorService.shutdown();
    }

    private static void sendBookingRequest(
            HttpClient httpClient,
            CountDownLatch startSignal,
            String requestName,
            Long eventId,
            Integer numberOfTickets
    ) {
        try {
            startSignal.await();

            String requestBody = """
                    {
                      "eventId": %d,
                      "numberOfTickets": %d
                      
                    }
                    """.formatted(eventId, numberOfTickets);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + JWT_TOKEN)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            System.out.println("========== " + requestName + " ==========");
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response: " + response.body());

        } catch (Exception e) {
            System.out.println(requestName + " failed: " + e.getMessage());
        }
    }
}
