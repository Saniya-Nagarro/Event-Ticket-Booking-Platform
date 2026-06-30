package com.event.ticketing.notification_service.event;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserRegisteredEvent {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private LocalDateTime registeredAt;
}
