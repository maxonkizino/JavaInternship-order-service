package com.javainternshiporderservice.dto.request.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateItemRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @NotNull
    @Positive
    private BigDecimal price;

    private boolean active = true;

}
