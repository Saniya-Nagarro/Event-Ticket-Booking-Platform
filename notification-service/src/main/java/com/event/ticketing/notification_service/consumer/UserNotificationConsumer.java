package com.event.ticketing.notification_service.consumer;

import com.event.ticketing.notification_service.event.UserRegisteredEvent;
import com.event.ticketing.notification_service.event.UserRoleChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserNotificationConsumer {

    @KafkaListener(
            topics = "user-registered-events",
            groupId = "notification-service",
            containerFactory = "userRegisteredKafkaListenerContainerFactory"
    )
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Notification: Welcome user. userId={}, name={}, email={}, role={}",
                event.getUserId(),
                event.getName(),
                event.getEmail(),
                event.getRole());
    }

    @KafkaListener(
            topics = "user-role-changed-events",
            groupId = "notification-service",
            containerFactory = "userRoleChangedKafkaListenerContainerFactory"
    )
    public void handleUserRoleChanged(UserRoleChangedEvent event) {
        log.info("Notification: User role changed. userId={}, email={}, oldRole={}, newRole={}",
                event.getUserId(),
                event.getEmail(),
                event.getOldRole(),
                event.getNewRole());
    }
}
