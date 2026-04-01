package com.javainternshiporderservice.dto.request.update;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class UpdateOrderRequest {

    private UUID userId;

    @Size(min = 2, max = 50)
    private String status;

    @Positive
    private BigDecimal totalPrice;

    @Valid
    private List<UpdateOrderItemRequest> orderItems;

}
