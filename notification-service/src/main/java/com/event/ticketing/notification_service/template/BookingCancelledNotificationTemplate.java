package com.event.ticketing.notification_service.template;

import org.springframework.stereotype.Service;

import com.event.ticketing.notification_service.NotificationService;
import com.event.ticketing.notification_service.event.BookingCancelledEvent;
import com.event.ticketing.notification_service.strategy.NotificationType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCancelledNotificationTemplate extends AbstractNotificationTemplate<BookingCancelledEvent> {

	private final NotificationService notificationService;

	@Override
	protected String getRecipient(BookingCancelledEvent event) {
		return event.getUserEmail();
	}

	@Override
	protected String buildSubject(BookingCancelledEvent event) {
		return "Booking Cancelled";
	}

	@Override
	protected String buildMessage(BookingCancelledEvent event) {
		return "Booking cancelled. bookingId=" + event.getBookingId() + ", eventId=" + event.getEventId() + ", tickets="
				+ event.getNumberOfTickets();
	}

	@Override
	protected void sendNotification(String recipient, String subject, String message) {
		notificationService.sendNotification(NotificationType.LOG, recipient, subject, message);
	}

	@Override
	protected void logResult(BookingCancelledEvent event) {
		log.info("Booking cancelled notification processed bookingId={} userEmail={}", event.getBookingId(),
				event.getUserEmail());
	}
}