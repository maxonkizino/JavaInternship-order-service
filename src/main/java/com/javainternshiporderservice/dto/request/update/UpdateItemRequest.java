package com.javainternshiporderservice.dto.request.update;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateItemRequest {

    @Size(min = 2, max = 255)
    private String name;

    @Positive
    private BigDecimal price;

}
