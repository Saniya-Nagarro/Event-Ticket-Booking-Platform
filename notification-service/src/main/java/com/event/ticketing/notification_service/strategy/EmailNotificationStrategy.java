package com.event.ticketing.notification_service.strategy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailNotificationStrategy implements NotificationStrategy {

	@Override
	public NotificationType getType() {
		return NotificationType.EMAIL;
	}

	@Override
	public void send(String recipient, String subject, String message) {
		log.info("EMAIL Notification sentTo={} subject={} message={}", recipient, subject, message);
	}
}