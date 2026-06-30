package com.event.ticketing.notification_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookingCancelledEvent {
    private Long bookingId;
    private Long eventId;
    private String correlationId;
    private String userEmail;
    private Integer numberOfTickets;
    private LocalDateTime cancelledAt;
}
