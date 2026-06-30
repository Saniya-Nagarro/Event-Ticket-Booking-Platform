package com.event.ticketing.eventservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {

    private Long eventId;
    private Integer availableSeats;
    private Boolean available;
}
