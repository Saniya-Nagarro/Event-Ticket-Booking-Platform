package com.event.ticketing.notification_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EventCancelledEvent {
    private Long eventId;
    private String eventName;
    private String correlationId;
    private String venue;
    private LocalDateTime eventDateTime;
    private LocalDateTime cancelledAt;
}
