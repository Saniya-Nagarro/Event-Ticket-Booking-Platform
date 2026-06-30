package com.event.ticketing.eventservice.service.impl.test;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.event.ticketing.eventservice.dto.AvailabilityResponse;
import com.event.ticketing.eventservice.dto.CreateEventRequest;
import com.event.ticketing.eventservice.dto.EventResponse;
import com.event.ticketing.eventservice.dto.UpdateEventRequest;
import com.event.ticketing.eventservice.entity.Event;
import com.event.ticketing.eventservice.enums.EventStatus;
import com.event.ticketing.eventservice.events.EventCancelledEvent;
import com.event.ticketing.eventservice.producer.EventKafkaProducer;
import com.event.ticketing.eventservice.repository.EventRepository;
import com.event.ticketing.eventservice.service.impl.EventServiceImpl;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventKafkaProducer eventKafkaProducer;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event() {
        return Event.builder()
                .id(1L)
                .name("Java Conference")
                .description("Spring Boot Event")
                .venue("Delhi")
                .eventDateTime(LocalDateTime.of(2026, 8, 1, 10, 0))
                .totalSeats(100)
                .availableSeats(100)
                .ticketPrice(BigDecimal.valueOf(999))
                .status(EventStatus.PROPOSED)
                .build();
    }

    @Test
    void createEvent_ShouldCreateEventSuccessfully() {
        CreateEventRequest request = new CreateEventRequest();
        request.setName("Java Conference");
        request.setDescription("Spring Boot Event");
        request.setVenue("Delhi");
        request.setEventDateTime(LocalDateTime.of(2026, 8, 1, 10, 0));
        request.setTotalSeats(100);
        request.setTicketPrice(BigDecimal.valueOf(999));

        when(eventRepository.save(any(Event.class)))
                .thenAnswer(invocation -> {
                    Event e = invocation.getArgument(0);
                    e.setId(1L);
                    return e;
                });

        EventResponse response = eventService.createEvent(request);

        assertEquals(1L, response.getId());
        assertEquals("Java Conference", response.getName());
        assertEquals(100, response.getTotalSeats());
        assertEquals(100, response.getAvailableSeats());
        assertEquals(EventStatus.PROPOSED, response.getStatus());

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_ShouldUpdateEventSuccessfully() {
        Event event = event();

        UpdateEventRequest request = new UpdateEventRequest();
        request.setName("Updated Event");
        request.setVenue("Noida");
        request.setTotalSeats(120);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventResponse response = eventService.updateEvent(1L, request);

        assertEquals("Updated Event", response.getName());
        assertEquals("Noida", response.getVenue());
        assertEquals(120, response.getTotalSeats());
        assertEquals(120, response.getAvailableSeats());

        verify(eventRepository).save(event);
    }

    @Test
    void updateEvent_ShouldThrowException_WhenEventCancelled() {
        Event event = event();
        event.setStatus(EventStatus.CANCELLED);

        UpdateEventRequest request = new UpdateEventRequest();
        request.setName("Updated Event");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.updateEvent(1L, request)
        );

        assertEquals("Cancelled event cannot be updated", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_ShouldThrowException_WhenTotalSeatsLessThanBookedSeats() {
        Event event = event();
        event.setTotalSeats(100);
        event.setAvailableSeats(60);

        UpdateEventRequest request = new UpdateEventRequest();
        request.setTotalSeats(30);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.updateEvent(1L, request)
        );

        assertEquals("Total seats cannot be less than already booked seats", ex.getMessage());
    }

    @Test
    void publishEvent_ShouldPublishEventSuccessfully() {
        Event event = event();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventResponse response = eventService.publishEvent(1L);

        assertEquals(EventStatus.PUBLISHED, response.getStatus());
        verify(eventRepository).save(event);
    }

    @Test
    void publishEvent_ShouldThrowException_WhenEventCancelled() {
        Event event = event();
        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.publishEvent(1L)
        );

        assertEquals("Cancelled event cannot be published", ex.getMessage());
    }

    @Test
    void cancelEvent_ShouldCancelEventAndPublishKafkaEvent() {
        Event event = event();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventResponse response = eventService.cancelEvent(1L);

        assertEquals(EventStatus.CANCELLED, response.getStatus());

        ArgumentCaptor<EventCancelledEvent> captor =
                ArgumentCaptor.forClass(EventCancelledEvent.class);

        verify(eventKafkaProducer).publishEventCancelled(captor.capture());

        EventCancelledEvent publishedEvent = captor.getValue();

        assertEquals(1L, publishedEvent.getEventId());
        assertEquals("Java Conference", publishedEvent.getEventName());
        assertEquals("Delhi", publishedEvent.getVenue());
    }

    @Test
    void getAllPublishedEvents_ShouldReturnOnlyPublishedEvents() {
        Event event = event();
        event.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findByStatus(EventStatus.PUBLISHED))
                .thenReturn(List.of(event));

        List<EventResponse> response = eventService.getAllPublishedEvents();

        assertEquals(1, response.size());
        assertEquals(EventStatus.PUBLISHED, response.get(0).getStatus());
    }

    @Test
    void getEventById_ShouldReturnEvent() {
        Event event = event();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventResponse response = eventService.getEventById(1L);

        assertEquals(1L, response.getId());
        assertEquals("Java Conference", response.getName());
    }

    @Test
    void getEventById_ShouldThrowException_WhenEventNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.getEventById(1L)
        );

        assertEquals("Event not found with id: 1", ex.getMessage());
    }

    @Test
    void searchEvents_ShouldReturnDistinctEvents() {
        Event event = event();
        event.setStatus(EventStatus.PUBLISHED);

        when(eventRepository.findByNameContainingIgnoreCaseAndStatus("java", EventStatus.PUBLISHED))
                .thenReturn(List.of(event));

        when(eventRepository.findByVenueContainingIgnoreCaseAndStatus("java", EventStatus.PUBLISHED))
                .thenReturn(List.of(event));

        List<EventResponse> response = eventService.searchEvents("java");

        assertEquals(1, response.size());
    }

    @Test
    void checkAvailability_ShouldReturnAvailableTrue_WhenPublishedAndSeatsAvailable() {
        Event event = event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setAvailableSeats(10);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        AvailabilityResponse response = eventService.checkAvailability(1L);

        assertEquals(1L, response.getEventId());
        assertEquals(10, response.getAvailableSeats());
        assertTrue(response.getAvailable());
    }

    @Test
    void reduceSeats_ShouldReduceSeatsSuccessfully() {
        Event event = event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setAvailableSeats(10);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventService.reduceSeats(1L, 2);

        assertEquals(8, event.getAvailableSeats());
        verify(eventRepository).save(event);
    }

    @Test
    void reduceSeats_ShouldThrowException_WhenCountInvalid() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.reduceSeats(1L, 0)
        );

        assertEquals("Seat count must be greater than zero", ex.getMessage());
    }

    @Test
    void reduceSeats_ShouldThrowException_WhenEventNotPublished() {
        Event event = event();
        event.setStatus(EventStatus.PROPOSED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.reduceSeats(1L, 2)
        );

        assertEquals("Event is not published", ex.getMessage());
    }

    @Test
    void reduceSeats_ShouldThrowException_WhenNotEnoughSeats() {
        Event event = event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setAvailableSeats(1);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.reduceSeats(1L, 2)
        );

        assertEquals("Not enough seats available", ex.getMessage());
    }

    @Test
    void increaseSeats_ShouldIncreaseSeatsSuccessfully() {
        Event event = event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setAvailableSeats(8);
        event.setTotalSeats(10);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        eventService.increaseSeats(1L, 2);

        assertEquals(10, event.getAvailableSeats());
        verify(eventRepository).save(event);
    }

    @Test
    void increaseSeats_ShouldThrowException_WhenCountInvalid() {
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.increaseSeats(1L, 0)
        );

        assertEquals("Seat count must be greater than zero", ex.getMessage());
    }

    @Test
    void increaseSeats_ShouldThrowException_WhenEventCancelled() {
        Event event = event();
        event.setStatus(EventStatus.CANCELLED);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.increaseSeats(1L, 2)
        );

        assertEquals("Cannot increase seats for cancelled event", ex.getMessage());
    }

    @Test
    void increaseSeats_ShouldThrowException_WhenAvailableSeatsExceedTotalSeats() {
        Event event = event();
        event.setStatus(EventStatus.PUBLISHED);
        event.setAvailableSeats(9);
        event.setTotalSeats(10);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> eventService.increaseSeats(1L, 2)
        );

        assertEquals("Available seats cannot exceed total seats", ex.getMessage());
    }
}
