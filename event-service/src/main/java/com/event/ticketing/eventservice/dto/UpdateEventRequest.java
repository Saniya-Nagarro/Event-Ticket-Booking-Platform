package com.event.ticketing.eventservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateEventRequest {

    private String name;

    private String description;

    private String venue;

    @Future(message = "Event date and time must be in the future")
    private LocalDateTime eventDateTime;

    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;

    @DecimalMin(value = "0.0", inclusive = true, message = "Ticket price cannot be negative")
    private BigDecimal ticketPrice;
}
