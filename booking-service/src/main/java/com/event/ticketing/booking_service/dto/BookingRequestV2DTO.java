package com.event.ticketing.booking_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequestV2DTO {

	@NotNull(message = "Event ID is required")
	private Long eventId;

	@NotNull(message = "Number of tickets is required")
	@Min(value = 1, message = "At least one ticket must be booked")
	private Integer numberOfTickets;

	private String couponCode;

	private String paymentMode;
}