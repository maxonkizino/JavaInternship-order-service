package com.javainternshiporderservice.dto.request.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateOrderItemRequest {

    @NotNull
    private UUID itemId;

    @NotNull
    @Positive
    private Integer quantity;

    private boolean active = true;

}
