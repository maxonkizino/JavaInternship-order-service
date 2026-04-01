package com.javainternshiporderservice.service;

import org.springframework.stereotype.Service;
import com.javainternshiporderservice.dto.response.OrderResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;

@Service
public interface OrderService {

    OrderResponse getOrderById(UUID id);
    OrderResponse getOrderByUserId(UUID userId);
    Page<OrderResponse> getAllOrders(Pageable pageable);


    
    void createOrder(CreateOrderRequest createOrderRequest);
    void updateOrder(UpdateOrderRequest updateOrderRequest);
    void activateOrder(UUID id);
    void deactivateOrder(UUID id);
}
