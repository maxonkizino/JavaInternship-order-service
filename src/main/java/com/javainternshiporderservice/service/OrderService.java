package com.javainternshiporderservice.service;

import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service interface for managing orders.
 * Provides CRUD operations and status management for orders.
 */
@Service
public interface OrderService {

    /**
     * Retrieves an order by its ID.
     *
     * @param id the UUID of the order
     * @return OrderResponse containing order details
     * @throws com.javainternshiporderservice.exception.OrderNotFoundException if order not found
     */
    OrderResponse getOrderById(UUID id);

    /**
     * Retrieves an order by user ID.
     *
     * @param userId the UUID of the user
     * @return OrderResponse containing order details
     * @throws com.javainternshiporderservice.exception.OrderNotFoundException if order not found
     */
    OrderResponse getOrderByUserId(UUID userId);

    /**
     * Retrieves all active orders with pagination.
     *
     * @param pageable pagination information
     * @return Page of OrderResponse
     */
    Page<OrderResponse> getAllOrders(Pageable pageable);

    /**
     * Creates a new order.
     *
     * @param createOrderRequest the order creation request
     * @return OrderResponse of the created order
     */
    OrderResponse createOrder(CreateOrderRequest createOrderRequest);

    /**
     * Updates an existing order.
     *
     * @param updateOrderRequest the order update request
     * @return OrderResponse of the updated order
     * @throws com.javainternshiporderservice.exception.OrderNotFoundException if order not found
     */
    OrderResponse updateOrder(UpdateOrderRequest updateOrderRequest);

    /**
     * Activates (soft restore) an order by ID.
     *
     * @param id the UUID of the order
     */
    void activateOrder(UUID id);

    /**
     * Deactivates (soft delete) an order by ID.
     *
     * @param id the UUID of the order
     */
    void deactivateOrder(UUID id);
}
