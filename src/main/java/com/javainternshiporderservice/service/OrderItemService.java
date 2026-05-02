package com.javainternshiporderservice.service;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service interface for managing order items.
 * Provides CRUD operations for order item entities.
 */
@Service
public interface OrderItemService {

    /**
     * Retrieves an order item by its ID.
     *
     * @param id the UUID of the order item
     * @return OrderItemResponse containing order item details
     * @throws com.javainternshiporderservice.exception.OrderItemNotFoundException if order item not found
     */
    OrderItemResponse getOrderItemById(UUID id);

    /**
     * Retrieves an order item by order ID.
     *
     * @param orderId the UUID of the order
     * @return OrderItemResponse containing order item details
     * @throws com.javainternshiporderservice.exception.OrderItemNotFoundException if order item not found
     */
    OrderItemResponse getOrderItemByOrderId(UUID orderId);

    /**
     * Retrieves an order item by item ID.
     *
     * @param itemId the UUID of the item
     * @return OrderItemResponse containing order item details
     * @throws com.javainternshiporderservice.exception.OrderItemNotFoundException if order item not found
     */
    OrderItemResponse getOrderItemByItemId(UUID itemId);

    /**
     * Retrieves all active order items with pagination.
     *
     * @param pageable pagination information
     * @return Page of OrderItemResponse
     */
    Page<OrderItemResponse> getAllOrderItems(Pageable pageable);

    /**
     * Retrieves order items with dynamic filtering.
     *
     * @param active filter by active status
     * @param orderId filter by order ID
     * @param itemId filter by item ID
     * @param pageable pagination information
     * @return Page of OrderItemResponse
     */
    Page<OrderItemResponse> getOrderItemsWithFilter(
            Boolean active,
            UUID orderId,
            UUID itemId,
            Pageable pageable);

    /**
     * Creates a new order item.
     *
     * @param createOrderItemRequest the order item creation request
     * @return OrderItemResponse of the created order item
     */
    OrderItemResponse createOrderItem(CreateOrderItemRequest createOrderItemRequest);

    /**
     * Updates an existing order item.
     *
     * @param updateOrderItemRequest the order item update request
     * @return OrderItemResponse of the updated order item
     * @throws com.javainternshiporderservice.exception.OrderItemNotFoundException if order item not found
     */
    OrderItemResponse updateOrderItem(UpdateOrderItemRequest updateOrderItemRequest);

    /**
     * Activates (soft restore) an order item by ID.
     *
     * @param id the UUID of the order item
     */
    void activateOrderItem(UUID id);

    /**
     * Deactivates (soft delete) an order item by ID.
     *
     * @param id the UUID of the order item
     */
    void deactivateOrderItem(UUID id);
}
