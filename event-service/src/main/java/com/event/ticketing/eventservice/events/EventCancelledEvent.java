package com.event.ticketing.eventservice.events;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCancelledEvent {

	private Long eventId;
	private String eventName;
	private String eventType;
	private String correlationId;
	private String venue;
	private LocalDateTime eventDateTime;
	private LocalDateTime cancelledAt;
}