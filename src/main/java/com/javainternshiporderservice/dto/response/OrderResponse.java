package com.javainternshiporderservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponse {

    private UUID id;
    private Long userId;
    private String status;
    private BigDecimal totalPrice;
    /** Soft-delete flag: {@code true} if the order is deleted. */
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> orderItems;

}
