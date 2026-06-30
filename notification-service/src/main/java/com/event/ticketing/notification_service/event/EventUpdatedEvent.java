package com.event.ticketing.notification_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class EventUpdatedEvent {
    private Long eventId;
    private String eventName;
    private String venue;
    private LocalDateTime eventDateTime;
    private LocalDateTime updatedAt;
}
