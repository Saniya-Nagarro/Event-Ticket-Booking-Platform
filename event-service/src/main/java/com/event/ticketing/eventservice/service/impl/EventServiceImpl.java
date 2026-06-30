package com.event.ticketing.eventservice.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.event.ticketing.eventservice.dto.AvailabilityResponse;
import com.event.ticketing.eventservice.dto.CreateEventRequest;
import com.event.ticketing.eventservice.dto.EventResponse;
import com.event.ticketing.eventservice.dto.PatchEventRequest;
import com.event.ticketing.eventservice.dto.UpdateEventRequest;
import com.event.ticketing.eventservice.entity.Event;
import com.event.ticketing.eventservice.enums.EventStatus;
import com.event.ticketing.eventservice.events.EventCancelledEvent;
import com.event.ticketing.eventservice.producer.EventKafkaProducer;
import com.event.ticketing.eventservice.repository.EventRepository;
import com.event.ticketing.eventservice.service.EventService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final String CORRELATION_ID = "correlationId";

    private final EventRepository eventRepository;
    private final EventKafkaProducer eventKafkaProducer;

    @Override
    public EventResponse createEvent(CreateEventRequest request) {
        log.info("Create event started name={} venue={} totalSeats={}",
                request.getName(), request.getVenue(), request.getTotalSeats());

        Event event = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .venue(request.getVenue())
                .eventDateTime(request.getEventDateTime())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .ticketPrice(request.getTicketPrice())
                .status(EventStatus.PROPOSED)
                .build();

        Event savedEvent = eventRepository.save(event);

        log.info("Create event successful eventId={} name={} status={}",
                savedEvent.getId(), savedEvent.getName(), savedEvent.getStatus());

        return mapToResponse(savedEvent);
    }

    @Override
    public EventResponse updateEvent(Long id, UpdateEventRequest request) {
        log.info("Update event started eventId={}", id);

        Event event = getEvent(id);

        if (event.getStatus() == EventStatus.CANCELLED) {
            log.warn("Update event failed reason=cancelled_event eventId={}", id);
            throw new RuntimeException("Cancelled event cannot be updated");
        }

        applyFullUpdate(event, request);

        Event savedEvent = eventRepository.save(event);

        log.info("Update event successful eventId={} status={} totalSeats={} availableSeats={}",
                savedEvent.getId(), savedEvent.getStatus(),
                savedEvent.getTotalSeats(), savedEvent.getAvailableSeats());

        return mapToResponse(savedEvent);
    }

    @Override
    @Transactional
    public EventResponse patchEvent(Long id, PatchEventRequest request) {
        log.info("Patch event started eventId={} venueProvided={} ticketPriceProvided={}",
                id, request.getVenue() != null, request.getTicketPrice() != null);

        Event event = getEvent(id);

        if (event.getStatus() == EventStatus.CANCELLED) {
            log.warn("Patch event failed reason=cancelled_event eventId={}", id);
            throw new RuntimeException("Cancelled event cannot be updated");
        }

        if (request.getVenue() != null) {
            event.setVenue(request.getVenue());
        }

        if (request.getTicketPrice() != null) {
            event.setTicketPrice(request.getTicketPrice());
        }

        Event savedEvent = eventRepository.save(event);

        log.info("Patch event successful eventId={} venue={} ticketPrice={}",
                savedEvent.getId(), savedEvent.getVenue(), savedEvent.getTicketPrice());

        return mapToResponse(savedEvent);
    }

    @Override
    public EventResponse publishEvent(Long id) {
        log.info("Publish event started eventId={}", id);

        Event event = getEvent(id);

        if (event.getStatus() == EventStatus.CANCELLED) {
            log.warn("Publish event failed reason=cancelled_event eventId={}", id);
            throw new RuntimeException("Cancelled event cannot be published");
        }

        event.setStatus(EventStatus.PUBLISHED);

        Event savedEvent = eventRepository.save(event);

        log.info("Publish event successful eventId={} status={}",
                savedEvent.getId(), savedEvent.getStatus());

        return mapToResponse(savedEvent);
    }

    @Override
    public EventResponse cancelEvent(Long id) {
        log.info("Cancel event started eventId={}", id);

        Event event = getEvent(id);

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);

        eventKafkaProducer.publishEventCancelled(
                EventCancelledEvent.builder()
                        .eventType("EVENT_CANCELLED")
                        .correlationId(MDC.get(CORRELATION_ID))
                        .eventId(event.getId())
                        .eventName(event.getName())
                        .venue(event.getVenue())
                        .eventDateTime(event.getEventDateTime())
                        .cancelledAt(LocalDateTime.now())
                        .build()
        );

        log.info("Cancel event successful eventId={} eventName={}",
                event.getId(), event.getName());

        log.info("Kafka event published eventType=EVENT_CANCELLED eventId={} correlationId={}",
                event.getId(), MDC.get(CORRELATION_ID));

        return mapToResponse(event);
    }

    @Override
    public List<EventResponse> getAllPublishedEvents() {
        log.info("Fetch all published events started");

        List<EventResponse> events = eventRepository.findByStatus(EventStatus.PUBLISHED)
                .stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Fetch all published events successful count={}", events.size());

        return events;
    }

    @Override
    public EventResponse getEventById(Long id) {
        log.info("Fetch event by id started eventId={}", id);

        Event event = getEvent(id);

        log.info("Fetch event by id successful eventId={} status={} availableSeats={}",
                event.getId(), event.getStatus(), event.getAvailableSeats());

        return mapToResponse(event);
    }

    @Override
    public List<EventResponse> searchEvents(String keyword) {
        log.info("Search events started keyword={}", keyword);

        List<Event> byName = eventRepository.findByNameContainingIgnoreCaseAndStatus(
                keyword, EventStatus.PUBLISHED);

        List<Event> byVenue = eventRepository.findByVenueContainingIgnoreCaseAndStatus(
                keyword, EventStatus.PUBLISHED);

        List<EventResponse> events = Stream.concat(byName.stream(), byVenue.stream())
                .distinct()
                .map(this::mapToResponse)
                .toList();

        log.info("Search events successful keyword={} count={}", keyword, events.size());

        return events;
    }

    @Override
    public AvailabilityResponse checkAvailability(Long id) {
        log.info("Check availability started eventId={}", id);

        Event event = getEvent(id);

        boolean available = event.getStatus() == EventStatus.PUBLISHED
                && event.getAvailableSeats() > 0;

        log.info("Check availability successful eventId={} availableSeats={} available={}",
                event.getId(), event.getAvailableSeats(), available);

        return AvailabilityResponse.builder()
                .eventId(event.getId())
                .availableSeats(event.getAvailableSeats())
                .available(available)
                .build();
    }

    @Override
    @Transactional
    public void reduceSeats(Long id, Integer count) {
        log.info("Reduce seats started eventId={} requestedSeats={}", id, count);

        if (count == null || count <= 0) {
            log.warn("Reduce seats failed reason=invalid_count eventId={} requestedSeats={}", id, count);
            throw new RuntimeException("Seat count must be greater than zero");
        }

        Event event = getEvent(id);

        if (event.getStatus() != EventStatus.PUBLISHED) {
            log.warn("Reduce seats failed reason=event_not_published eventId={} status={}",
                    id, event.getStatus());
            throw new RuntimeException("Event is not published");
        }

        if (event.getAvailableSeats() < count) {
            log.warn("Reduce seats failed reason=not_enough_seats eventId={} availableSeats={} requestedSeats={}",
                    id, event.getAvailableSeats(), count);
            throw new RuntimeException("Not enough seats available");
        }

        int beforeSeats = event.getAvailableSeats();
        event.setAvailableSeats(event.getAvailableSeats() - count);
        eventRepository.save(event);

        log.info("Reduce seats successful eventId={} beforeSeats={} reducedBy={} afterSeats={}",
                id, beforeSeats, count, event.getAvailableSeats());
    }

    @Override
    @Transactional
    public void increaseSeats(Long id, Integer count) {
        log.info("Increase seats started eventId={} seatsToIncrease={}", id, count);

        if (count == null || count <= 0) {
            log.warn("Increase seats failed reason=invalid_count eventId={} seatsToIncrease={}", id, count);
            throw new RuntimeException("Seat count must be greater than zero");
        }

        Event event = getEvent(id);

        if (event.getStatus() == EventStatus.CANCELLED) {
            log.warn("Increase seats failed reason=cancelled_event eventId={}", id);
            throw new RuntimeException("Cannot increase seats for cancelled event");
        }

        if (event.getAvailableSeats() + count > event.getTotalSeats()) {
            log.warn("Increase seats failed reason=available_exceeds_total eventId={} availableSeats={} increaseBy={} totalSeats={}",
                    id, event.getAvailableSeats(), count, event.getTotalSeats());
            throw new RuntimeException("Available seats cannot exceed total seats");
        }

        int beforeSeats = event.getAvailableSeats();
        event.setAvailableSeats(event.getAvailableSeats() + count);
        eventRepository.save(event);

        log.info("Increase seats successful eventId={} beforeSeats={} increasedBy={} afterSeats={}",
                id, beforeSeats, count, event.getAvailableSeats());
    }

    private Event getEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Event not found eventId={}", id);
                    return new RuntimeException("Event not found with id: " + id);
                });
    }

    private void applyFullUpdate(Event event, UpdateEventRequest request) {
        if (request.getName() != null) {
            event.setName(request.getName());
        }

        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }

        if (request.getVenue() != null) {
            event.setVenue(request.getVenue());
        }

        if (request.getEventDateTime() != null) {
            event.setEventDateTime(request.getEventDateTime());
        }

        if (request.getTicketPrice() != null) {
            event.setTicketPrice(request.getTicketPrice());
        }

        if (request.getTotalSeats() != null) {
            int bookedSeats = event.getTotalSeats() - event.getAvailableSeats();

            if (request.getTotalSeats() < bookedSeats) {
                log.warn("Update event failed reason=total_seats_less_than_booked eventId={} requestedTotalSeats={} bookedSeats={}",
                        event.getId(), request.getTotalSeats(), bookedSeats);
                throw new RuntimeException("Total seats cannot be less than already booked seats");
            }

            event.setTotalSeats(request.getTotalSeats());
            event.setAvailableSeats(request.getTotalSeats() - bookedSeats);
        }
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .venue(event.getVenue())
                .eventDateTime(event.getEventDateTime())
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .ticketPrice(event.getTicketPrice())
                .status(event.getStatus())
                .build();
    }
}