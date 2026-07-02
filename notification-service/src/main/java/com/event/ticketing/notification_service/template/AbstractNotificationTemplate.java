package com.event.ticketing.notification_service.template;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractNotificationTemplate<T> {

    public final void process(T event) {
        validate(event);

        String recipient = getRecipient(event);
        String subject = buildSubject(event);
        String message = buildMessage(event);

        sendNotification(recipient, subject, message);

        logResult(event);
    }

    protected void validate(T event) {
        if (event == null) {
            throw new RuntimeException("Notification event cannot be null");
        }
    }

    protected abstract String getRecipient(T event);

    protected abstract String buildSubject(T event);

    protected abstract String buildMessage(T event);

    protected abstract void sendNotification(String recipient, String subject, String message);

    protected abstract void logResult(T event);
}