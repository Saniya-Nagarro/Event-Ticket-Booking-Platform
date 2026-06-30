package com.event.ticketing.notification_service.config;

import com.event.ticketing.notification_service.event.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private Map<String, Object> commonProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return props;
    }

    private <T> ConsumerFactory<String, T> consumerFactory(Class<T> targetType) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType, false);
       // deserializer.addTrustedPackages("*");
        return new DefaultKafkaConsumerFactory<>(
                commonProps(),
                new StringDeserializer(),
                deserializer
        );
    }

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> listenerFactory(Class<T> targetType) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(targetType));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingCreatedEvent>
    bookingCreatedKafkaListenerContainerFactory() {
        return listenerFactory(BookingCreatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingCancelledEvent>
    bookingCancelledKafkaListenerContainerFactory() {
        return listenerFactory(BookingCancelledEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BookingFailedEvent>
    bookingFailedKafkaListenerContainerFactory() {
        return listenerFactory(BookingFailedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventCreatedEvent>
    eventCreatedKafkaListenerContainerFactory() {
        return listenerFactory(EventCreatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventPublishedEvent>
    eventPublishedKafkaListenerContainerFactory() {
        return listenerFactory(EventPublishedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventUpdatedEvent>
    eventUpdatedKafkaListenerContainerFactory() {
        return listenerFactory(EventUpdatedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EventCancelledEvent>
    eventCancelledKafkaListenerContainerFactory() {
        return listenerFactory(EventCancelledEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRegisteredEvent>
    userRegisteredKafkaListenerContainerFactory() {
        return listenerFactory(UserRegisteredEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserRoleChangedEvent>
    userRoleChangedKafkaListenerContainerFactory() {
        return listenerFactory(UserRoleChangedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentSuccessfulEvent>
    paymentSuccessfulKafkaListenerContainerFactory() {
        return listenerFactory(PaymentSuccessfulEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentFailedEvent>
    paymentFailedKafkaListenerContainerFactory() {
        return listenerFactory(PaymentFailedEvent.class);
    }
}
