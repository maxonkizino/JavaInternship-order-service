package com.javainternshiporderservice.dto.response;

import lombok.Data;

@Data
public class OrderWithUserResponse {

    private OrderResponse order;
    private UserInfoResponse user;

}
