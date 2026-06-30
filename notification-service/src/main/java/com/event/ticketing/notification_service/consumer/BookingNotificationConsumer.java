package com.event.ticketing.notification_service.consumer;

import com.event.ticketing.notification_service.event.BookingCancelledEvent;
import com.event.ticketing.notification_service.event.BookingCreatedEvent;
import com.event.ticketing.notification_service.event.BookingFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BookingNotificationConsumer {

	private static final String CORRELATION_ID = "correlationId";

	@KafkaListener(topics = "booking-created-events", groupId = "notification-service", containerFactory = "bookingCreatedKafkaListenerContainerFactory")
	public void handleBookingCreated(BookingCreatedEvent event) {

		MDC.put(CORRELATION_ID, event.getCorrelationId());

		try {
			log.info("Notification: Booking confirmed. user={}, bookingId={}, eventId={}, tickets={}, amount={}",
					event.getUserEmail(), event.getBookingId(), event.getEventId(), event.getNumberOfTickets(),
					event.getTotalAmount());
		} finally {
			MDC.clear();
		}
	}

	@KafkaListener(topics = "booking-cancelled-events", groupId = "notification-service", containerFactory = "bookingCancelledKafkaListenerContainerFactory")
	public void handleBookingCancelled(BookingCancelledEvent event) {

		MDC.put(CORRELATION_ID, event.getCorrelationId());

		try {
			log.info("Notification: Booking cancelled. user={}, bookingId={}, eventId={}, tickets={}",
					event.getUserEmail(), event.getBookingId(), event.getEventId(), event.getNumberOfTickets());
		} finally {
			MDC.clear();
		}
	}

	@KafkaListener(topics = "booking-failed-events", groupId = "notification-service", containerFactory = "bookingFailedKafkaListenerContainerFactory")
	public void handleBookingFailed(BookingFailedEvent event) {

		MDC.put(CORRELATION_ID, event.getCorrelationId());

		try {
			log.info("Notification: Booking failed. user={}, eventId={}, tickets={}, reason={}", event.getUserEmail(),
					event.getEventId(), event.getNumberOfTickets(), event.getReason());
		} finally {
			MDC.clear();
		}
	}
}