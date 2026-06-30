package com.event.ticketing.notification_service.event;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookingCreatedEvent {
    private Long bookingId;
    private String correlationId;
    private Long eventId;
    private String userEmail;
    private Integer numberOfTickets;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
