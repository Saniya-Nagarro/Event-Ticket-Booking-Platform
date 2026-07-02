package com.event.ticketing.notification_service.strategy;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LogNotificationStrategy implements NotificationStrategy {

    @Override
    public NotificationType getType() {
        return NotificationType.LOG;
    }

    @Override
    public void send(String recipient, String subject, String message) {
        log.info("LOG Notification recipient={} subject={} message={}",
                recipient, subject, message);
    }
}