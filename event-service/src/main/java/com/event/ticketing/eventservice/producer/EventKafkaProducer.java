package com.event.ticketing.eventservice.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.event.ticketing.eventservice.events.EventCancelledEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishEventCancelled(EventCancelledEvent event) {
        kafkaTemplate.send("event-cancelled-events", event);
    }
}
