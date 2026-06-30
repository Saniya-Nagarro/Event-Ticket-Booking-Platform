package com.event.ticketing.notification_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserRoleChangedEvent {
    private Long userId;
    private String email;
    private String oldRole;
    private String newRole;
    private LocalDateTime changedAt;
}
