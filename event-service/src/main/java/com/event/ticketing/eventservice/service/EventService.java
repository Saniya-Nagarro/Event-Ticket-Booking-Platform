package com.event.ticketing.eventservice.service;

import java.util.List;

import com.event.ticketing.eventservice.dto.*;

public interface EventService {

	EventResponse createEvent(CreateEventRequest request);

	EventResponse updateEvent(Long id, UpdateEventRequest request);

	EventResponse patchEvent(Long id, PatchEventRequest request);

	EventResponse publishEvent(Long id);

	EventResponse cancelEvent(Long id);

	List<EventResponse> getAllPublishedEvents();

	EventResponse getEventById(Long id);

	List<EventResponse> searchEvents(String keyword);

	AvailabilityResponse checkAvailability(Long id);

	void reduceSeats(Long id, Integer count);

	void increaseSeats(Long id, Integer count);
}
