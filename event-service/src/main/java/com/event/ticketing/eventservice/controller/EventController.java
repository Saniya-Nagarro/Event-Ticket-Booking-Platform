package com.event.ticketing.eventservice.controller;

import com.event.ticketing.eventservice.dto.*;
import com.event.ticketing.eventservice.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

	private final EventService eventService;

	@PostMapping
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
	public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
	public ResponseEntity<EventResponse> updateEvent(@PathVariable Long id,
			@Valid @RequestBody UpdateEventRequest request) {
		return ResponseEntity.ok(eventService.updateEvent(id, request));
	}

	@PatchMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ResponseEntity<EventResponse> patchEvent(@PathVariable Long id, @RequestBody PatchEventRequest request) {

		return ResponseEntity.ok(eventService.patchEvent(id, request));
	}

	@PutMapping("/{id}/publish")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
	public ResponseEntity<EventResponse> publishEvent(@PathVariable Long id) {
		return ResponseEntity.ok(eventService.publishEvent(id));
	}

	@PutMapping("/{id}/cancel")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
	public ResponseEntity<EventResponse> cancelEvent(@PathVariable Long id) {
		return ResponseEntity.ok(eventService.cancelEvent(id));
	}

	@GetMapping
	public ResponseEntity<List<EventResponse>> getAllPublishedEvents() {
		return ResponseEntity.ok(eventService.getAllPublishedEvents());
	}

	@GetMapping("/{id}")
	public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
		return ResponseEntity.ok(eventService.getEventById(id));
	}

	@GetMapping("/search")
	public ResponseEntity<List<EventResponse>> searchEvents(@RequestParam String keyword) {
		return ResponseEntity.ok(eventService.searchEvents(keyword));
	}

	@GetMapping("/{id}/availability")
	public ResponseEntity<AvailabilityResponse> checkAvailability(@PathVariable Long id) {
		return ResponseEntity.ok(eventService.checkAvailability(id));
	}

	@PutMapping("/{id}/reduce-seats")
	public ResponseEntity<String> reduceSeats(@PathVariable Long id, @RequestParam Integer count) {
		eventService.reduceSeats(id, count);
		return ResponseEntity.ok("Seats reduced successfully");
	}

	@PutMapping("/{id}/increase-seats")
	public ResponseEntity<String> increaseSeats(@PathVariable Long id, @RequestParam Integer count) {
		eventService.increaseSeats(id, count);
		return ResponseEntity.ok("Seats increased successfully");
	}
}
