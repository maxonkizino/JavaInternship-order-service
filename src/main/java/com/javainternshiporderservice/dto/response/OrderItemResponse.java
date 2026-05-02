package com.javainternshiporderservice.dto.response;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class OrderItemResponse {

    private UUID id;
    private UUID orderId;
    private UUID itemId;
    private Integer quantity;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

}
