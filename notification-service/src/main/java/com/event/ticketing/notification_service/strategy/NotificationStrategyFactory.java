package com.event.ticketing.notification_service.strategy;


import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationStrategyFactory {

    private final List<NotificationStrategy> strategies;

    public NotificationStrategy getStrategy(NotificationType type) {
        return strategies.stream()
                .filter(strategy -> strategy.getType() == type)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Unsupported notification type: " + type));
    }
}