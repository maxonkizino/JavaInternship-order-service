package com.javainternshiporderservice.dto.request.update;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateItemRequest {

    @NotNull
    private UUID id;

    @Size(min = 2, max = 255)
    private String name;

    @Positive
    private BigDecimal price;

}
