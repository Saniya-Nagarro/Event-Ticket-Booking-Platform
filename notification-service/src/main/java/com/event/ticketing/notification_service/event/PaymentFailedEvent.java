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
public class PaymentFailedEvent {
    private Long bookingId;
    private String userEmail;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime failedAt;
}
