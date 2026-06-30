package com.event.ticketing.notification_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookingFailedEvent {
    private Long eventId;
    private String correlationId;
    private String userEmail;
    private Integer numberOfTickets;
    private String reason;
    private LocalDateTime failedAt;
}
