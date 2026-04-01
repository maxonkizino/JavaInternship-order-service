package com.javainternshiporderservice.dto.request.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetOrderStatusRequest {

    @NotBlank
    @Size(min = 2, max = 50)
    private String status;

}
