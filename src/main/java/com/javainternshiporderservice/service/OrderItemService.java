package com.javainternshiporderservice.service;

import org.springframework.stereotype.Service;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;


@Service
public interface OrderItemService {


    OrderItemResponse getOrderItemById(UUID id);
    OrderItemResponse getOrderItemByOrderId(UUID orderId);
    OrderItemResponse getOrderItemByItemId(UUID itemId);
    Page<OrderItemResponse> getAllOrderItems(Pageable pageable);



    
    void createOrderItem(CreateOrderItemRequest createOrderItemRequest);
    void updateOrderItem(UpdateOrderItemRequest updateOrderItemRequest);
    void activateOrderItem(UUID id);
    void deactivateOrderItem(UUID id);

}
