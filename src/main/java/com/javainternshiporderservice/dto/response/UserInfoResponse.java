package com.javainternshiporderservice.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoResponse {

    private Long id;
    private String name;
    private String surname;
    private String email;
    private LocalDate birthDate;
    private boolean active;

}
