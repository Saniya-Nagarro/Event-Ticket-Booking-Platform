package com.event.ticketing.eventservice.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PatchEventRequest {

    private String venue;
    private BigDecimal ticketPrice;
}