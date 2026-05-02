package com.javainternshiporderservice.event;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CreatePaymentEvent {

    private UUID paymentId;
    private UUID orderId;
    private String status;
    private Instant eventTime;
}
