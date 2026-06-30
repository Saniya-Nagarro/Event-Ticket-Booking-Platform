package com.event.ticketing.booking_service.client;

import com.event.ticketing.booking_service.dto.EventResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class EventClient {

    private final RestTemplate restTemplate;

    @Value("${services.event-service.base-url}")
    private String eventServiceBaseUrl;

    public EventResponseDTO getEventById(Long eventId) {

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        ResponseEntity<EventResponseDTO> response = restTemplate.exchange(
                eventServiceBaseUrl + "/" + eventId,
                HttpMethod.GET,
                entity,
                EventResponseDTO.class
        );

        return response.getBody();
    }

    public void reduceSeats(Long eventId, Integer count) {

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        restTemplate.exchange(
                eventServiceBaseUrl + "/" + eventId + "/reduce-seats?count=" + count,
                HttpMethod.PUT,
                entity,
                String.class
        );
    }

    public void increaseSeats(Long eventId, Integer count) {

        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        restTemplate.exchange(
                eventServiceBaseUrl + "/" + eventId + "/increase-seats?count=" + count,
                HttpMethod.PUT,
                entity,
                String.class
        );
    }

    private HttpHeaders authHeaders() {

        HttpHeaders headers = new HttpHeaders();

        // JWT Token
        Object credentials = SecurityContextHolder.getContext()
                .getAuthentication()
                .getCredentials();

        if (credentials instanceof String token && !token.isBlank()) {
            headers.setBearerAuth(token);
        }

        // Correlation ID
        String correlationId = MDC.get("correlationId");

        if (correlationId != null) {
            headers.set("X-Correlation-ID", correlationId);
        }

        return headers;
    }
}