package com.event.ticketing.booking_service.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BookingMetrics {

	private final Counter bookingCreatedCounter;
	private final Counter bookingCancelledCounter;
	private final Counter bookingFailedCounter;
	private final Counter errorCounter;

	public BookingMetrics(MeterRegistry meterRegistry) {
		this.bookingCreatedCounter = Counter.builder("booking_created_total").description("Total successful bookings")
				.register(meterRegistry);

		this.bookingCancelledCounter = Counter.builder("booking_cancelled_total")
				.description("Total cancelled bookings").register(meterRegistry);

		this.bookingFailedCounter = Counter.builder("booking_failed_total").description("Total failed booking attempts")
				.register(meterRegistry);

		this.errorCounter = Counter.builder("application_errors_total").description("Total application errors")
				.register(meterRegistry);
	}

	public void incrementBookingCreated() {
		bookingCreatedCounter.increment();
	}

	public void incrementBookingCancelled() {
		bookingCancelledCounter.increment();
	}

	public void incrementBookingFailed() {
		bookingFailedCounter.increment();
	}

	public void incrementError() {
		errorCounter.increment();
	}
}