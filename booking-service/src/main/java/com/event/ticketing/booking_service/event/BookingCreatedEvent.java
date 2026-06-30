package com.event.ticketing.booking_service.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCreatedEvent {
	private String eventType;
	private String correlationId;
	private Long bookingId;
	private Long eventId;
	private String userEmail;
	private Integer numberOfTickets;
	private BigDecimal totalAmount;
	private LocalDateTime createdAt;
}
