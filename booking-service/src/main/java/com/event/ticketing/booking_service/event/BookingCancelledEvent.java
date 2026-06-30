package com.event.ticketing.booking_service.event;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCancelledEvent {
	private String eventType;
	private String correlationId;
	private Long bookingId;
	private Long eventId;
	private String userEmail;
	private Integer numberOfTickets;
	private LocalDateTime cancelledAt;
}
