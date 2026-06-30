package com.event.ticketing.eventservice.repository;

import com.event.ticketing.eventservice.entity.Event;
import com.event.ticketing.eventservice.enums.EventStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    List<Event> findByNameContainingIgnoreCaseAndStatus(String keyword, EventStatus status);

    List<Event> findByVenueContainingIgnoreCaseAndStatus(String venue, EventStatus status);
}
