package com.event.ticketing.booking_service.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.event.ticketing.booking_service.event.BookingCancelledEvent;
import com.event.ticketing.booking_service.event.BookingCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void publishBookingCreated(BookingCreatedEvent event) {
		log.info("Publishing BOOKING_CREATED event: {}", event);
		kafkaTemplate.send("booking-created-events", event);
	}

	public void publishBookingCancelled(BookingCancelledEvent event) {
		log.info("Publishing BOOKING_CANCELLED event: {}", event);
		kafkaTemplate.send("booking-cancelled-events", event);
	}
}