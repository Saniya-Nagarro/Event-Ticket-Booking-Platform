package com.event.ticketing.notification_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/notifications/health")
    public String health() {
        return "notification-service is running";
    }
}
