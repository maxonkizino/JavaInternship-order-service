package com.javainternshiporderservice.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class ItemResponse {

    private UUID id;
    private String name;
    private BigDecimal price;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

}
