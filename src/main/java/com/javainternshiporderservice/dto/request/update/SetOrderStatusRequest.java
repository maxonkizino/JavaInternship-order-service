package com.javainternshiporderservice.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class SetOrderStatusRequest {

    @NotNull
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 50)
    private String status;

}
