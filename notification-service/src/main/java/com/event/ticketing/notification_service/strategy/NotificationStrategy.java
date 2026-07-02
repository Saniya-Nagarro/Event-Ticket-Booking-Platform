package com.event.ticketing.notification_service.strategy;

public interface NotificationStrategy {

	NotificationType getType();

	void send(String recipient, String subject, String message);
}