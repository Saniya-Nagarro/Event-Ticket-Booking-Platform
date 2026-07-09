package com.event.ticketing.booking_service.metrics;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class ActiveUserMetrics {

	private final Map<String, LocalDateTime> activeUsers = new ConcurrentHashMap<>();

	public ActiveUserMetrics(MeterRegistry meterRegistry) {
		Gauge.builder("active_users", activeUsers, Map::size).description("Current active users")
				.register(meterRegistry);
	}

	public void markActive(String email) {
		activeUsers.put(email, LocalDateTime.now());
	}

	@Scheduled(fixedRate = 60000) 
	public void removeInactiveUsers() {
		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
		activeUsers.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
	}
}
