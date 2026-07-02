package com.event.ticketing.notification_service;

import org.springframework.stereotype.Service;

import com.event.ticketing.notification_service.strategy.NotificationStrategyFactory;
import com.event.ticketing.notification_service.strategy.NotificationType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

	private final NotificationStrategyFactory notificationStrategyFactory;

	public void sendNotification(NotificationType type, String recipient, String subject, String message) {

		notificationStrategyFactory.getStrategy(type).send(recipient, subject, message);
	}
}