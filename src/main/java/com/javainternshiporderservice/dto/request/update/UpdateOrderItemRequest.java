package com.javainternshiporderservice.dto.request.update;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.UUID;


@Data
public class UpdateOrderItemRequest {

    @NotNull
    private UUID id;

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID itemId;

    @NotNull
    @Positive
    private Integer quantity;
}
