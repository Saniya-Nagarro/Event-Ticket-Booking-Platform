package com.event.ticketing.eventservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.event.ticketing.eventservice.enums.EventStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private String name;
    private String description;
    private String venue;
    private LocalDateTime eventDateTime;
    private Integer totalSeats;
    private Integer availableSeats;
    private BigDecimal ticketPrice;
    private EventStatus status;
}
