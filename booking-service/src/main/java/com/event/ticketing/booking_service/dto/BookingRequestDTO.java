package com.event.ticketing.booking_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequestDTO {
    @NotNull
    private Long eventId;

    @NotNull
    @Min(1)
    private Integer numberOfTickets;
}
