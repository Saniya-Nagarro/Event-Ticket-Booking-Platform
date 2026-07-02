package com.event.ticketing.notification_service.template;

import org.springframework.stereotype.Service;

import com.event.ticketing.notification_service.NotificationService;
import com.event.ticketing.notification_service.event.BookingCreatedEvent;
import com.event.ticketing.notification_service.strategy.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCreatedNotificationTemplate extends AbstractNotificationTemplate<BookingCreatedEvent> {

	private final NotificationService notificationService;

	@Override
	protected String getRecipient(BookingCreatedEvent event) {
		return event.getUserEmail();
	}

	@Override
	protected String buildSubject(BookingCreatedEvent event) {
		return "Booking Confirmed";
	}

	@Override
	protected String buildMessage(BookingCreatedEvent event) {
		return "Booking confirmed. bookingId=" + event.getBookingId() + ", eventId=" + event.getEventId() + ", tickets="
				+ event.getNumberOfTickets() + ", amount=" + event.getTotalAmount();
	}

	@Override
	protected void sendNotification(String recipient, String subject, String message) {
		notificationService.sendNotification(NotificationType.LOG, recipient, subject, message);
	}

	@Override
	protected void logResult(BookingCreatedEvent event) {
		log.info("Booking created notification processed bookingId={} userEmail={}", event.getBookingId(),
				event.getUserEmail());
	}
}