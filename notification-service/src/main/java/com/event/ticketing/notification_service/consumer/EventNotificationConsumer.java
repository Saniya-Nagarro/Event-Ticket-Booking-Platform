package com.event.ticketing.notification_service.consumer;

import com.event.ticketing.notification_service.event.*;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventNotificationConsumer {
	private static final String CORRELATION_ID = "correlationId";

	@KafkaListener(topics = "event-created-events", groupId = "notification-service", containerFactory = "eventCreatedKafkaListenerContainerFactory")
	public void handleEventCreated(EventCreatedEvent event) {
		MDC.put(CORRELATION_ID, event.getCorrelationId());
		try {
			log.info("Notification: Event created. eventId={}, name={}, venue={}, dateTime={}, createdBy={}",
					event.getEventId(), event.getEventName(), event.getVenue(), event.getEventDateTime(),
					event.getCreatedBy());
		} finally {
			MDC.clear();
		}
	}

	@KafkaListener(topics = "event-published-events", groupId = "notification-service", containerFactory = "eventPublishedKafkaListenerContainerFactory")
	public void handleEventPublished(EventPublishedEvent event) {
		log.info("Notification: New event is live. eventId={}, name={}, venue={}, dateTime={}", event.getEventId(),
				event.getEventName(), event.getVenue(), event.getEventDateTime());
	}

	@KafkaListener(topics = "event-updated-events", groupId = "notification-service", containerFactory = "eventUpdatedKafkaListenerContainerFactory")
	public void handleEventUpdated(EventUpdatedEvent event) {
		log.info("Notification: Event updated. eventId={}, name={}, venue={}, dateTime={}", event.getEventId(),
				event.getEventName(), event.getVenue(), event.getEventDateTime());
	}

	@KafkaListener(topics = "event-cancelled-events", groupId = "notification-service", containerFactory = "eventCancelledKafkaListenerContainerFactory")
	public void handleEventCancelled(EventCancelledEvent event) {
		MDC.put(CORRELATION_ID, event.getCorrelationId());
		try {
			log.info("Notification: Event cancelled. eventId={}, name={}, venue={}, dateTime={}", event.getEventId(),
					event.getEventName(), event.getVenue(), event.getEventDateTime());
		} finally {
			MDC.clear();
		}
	}
}
