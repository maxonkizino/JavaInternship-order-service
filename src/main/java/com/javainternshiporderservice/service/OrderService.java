package com.javainternshiporderservice.service;

import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public interface OrderService {

    OrderWithUserResponse getOrderById(UUID id);

    OrderWithUserResponse getOrderByUserId(Long userId);

    Page<OrderWithUserResponse> getAllOrders(Pageable pageable);

    OrderWithUserResponse createOrder(CreateOrderRequest createOrderRequest);

    OrderWithUserResponse updateOrder(UpdateOrderRequest updateOrderRequest);

    void activateOrder(UUID id);

    void deactivateOrder(UUID id);

    OrderWithUserResponse getOrderWithUserById(UUID orderId, String userEmail);
}
