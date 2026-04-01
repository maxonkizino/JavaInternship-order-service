package com.javainternshiporderservice.mapper;

import com.javainternshiporderservice.dto.response.OrderResponse;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.dto.response.UserInfoResponse;
import com.javainternshiporderservice.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderWithUserAssembler {

    private final OrderMapper orderMapper;

    public OrderWithUserResponse assemble(Order order, UserInfoResponse userInfo) {
        OrderWithUserResponse response = new OrderWithUserResponse();
        response.setOrder(orderMapper.toOrderResponse(order));
        response.setUser(userInfo);
        return response;
    }

}
