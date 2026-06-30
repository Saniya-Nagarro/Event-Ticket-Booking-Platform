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
public class PaymentSuccessfulEvent {
    private Long paymentId;
    private Long bookingId;
    private String userEmail;
    private BigDecimal amount;
    private LocalDateTime paidAt;
}
