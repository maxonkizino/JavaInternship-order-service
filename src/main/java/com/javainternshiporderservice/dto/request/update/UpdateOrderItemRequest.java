package com.javainternshiporderservice.dto.request.update;

import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateOrderItemRequest {

    private UUID id;

    private UUID itemId;

    @Positive
    private Integer quantity;

}
