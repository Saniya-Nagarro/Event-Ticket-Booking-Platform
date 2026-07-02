package com.event.ticketing.notification_service.consumer;

import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.event.ticketing.notification_service.event.BookingCancelledEvent;
import com.event.ticketing.notification_service.event.BookingCreatedEvent;
import com.event.ticketing.notification_service.template.BookingCancelledNotificationTemplate;
import com.event.ticketing.notification_service.template.BookingCreatedNotificationTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingNotificationConsumer {

	private static final String CORRELATION_ID = "correlationId";

	private final BookingCreatedNotificationTemplate bookingCreatedNotificationTemplate;
	private final BookingCancelledNotificationTemplate bookingCancelledNotificationTemplate;
	// private final BookingFailedNotificationTemplate
	// bookingFailedNotificationTemplate;

	@KafkaListener(topics = "booking-created-events", groupId = "notification-service", containerFactory = "bookingCreatedKafkaListenerContainerFactory")
	public void handleBookingCreated(BookingCreatedEvent event) {

		MDC.put(CORRELATION_ID, event.getCorrelationId());

		try {

			log.info("Received BOOKING_CREATED event bookingId={}", event.getBookingId());

			bookingCreatedNotificationTemplate.process(event);

		} finally {
			MDC.clear();
		}
	}

	@KafkaListener(topics = "booking-cancelled-events", groupId = "notification-service", containerFactory = "bookingCancelledKafkaListenerContainerFactory")
	public void handleBookingCancelled(BookingCancelledEvent event) {

		MDC.put(CORRELATION_ID, event.getCorrelationId());

		try {

			log.info("Received BOOKING_CANCELLED event bookingId={}", event.getBookingId());

			bookingCancelledNotificationTemplate.process(event);

		} finally {
			MDC.clear();
		}
	}

//	@KafkaListener(topics = "booking-failed-events", groupId = "notification-service", containerFactory = "bookingFailedKafkaListenerContainerFactory")
//	public void handleBookingFailed(BookingFailedEvent event) {
//
//		MDC.put(CORRELATION_ID, event.getCorrelationId());
//
//		try {
//
//			log.info("Received BOOKING_FAILED event eventId={}", event.getEventId());
//
//			bookingFailedNotificationTemplate.process(event);
//
//		} finally {
//			MDC.clear();
//		}
//	}
}