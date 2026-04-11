package com.javainternshiporderservice.dto.request.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(min = 2, max = 50)
    private String status;

    @NotNull
    @Positive
    private BigDecimal totalPrice;

    @NotEmpty
    @Valid
    private List<CreateOrderItemRequest> orderItems;

    /** Soft-delete: {@code false} for a new live order (DB column {@code deleted}). */
    private boolean deleted = false;

}
