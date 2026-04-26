package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import com.javainternshiporderservice.logging.ControllerLogger;
import com.javainternshiporderservice.service.OrderItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.UUID;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Validated
public class OrderItemController {

    private final OrderItemService orderItemService;
    private final ControllerLogger controllerLogger;

    @GetMapping("/filtered")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<OrderItemResponse>> getOrderItemsWithFilter(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) UUID itemId,
            Pageable pageable) {
        controllerLogger.methodCalled("OrderItemController", "getOrderItemsWithFilter", active, orderId, itemId);
        Page<OrderItemResponse> orderItems = orderItemService.getOrderItemsWithFilter(active, orderId, itemId, pageable);
        return ResponseEntity.ok(orderItems);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderItemResponse> getOrderItemById(@PathVariable UUID id) {
        controllerLogger.methodCalled("OrderItemController", "getOrderItemById", id);
        OrderItemResponse orderItem = orderItemService.getOrderItemById(id);
        return ResponseEntity.ok(orderItem);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<OrderItemResponse>> getAllOrderItems(Pageable pageable) {
        controllerLogger.methodCalled("OrderItemController", "getAllOrderItems");
        Page<OrderItemResponse> orderItems = orderItemService.getAllOrderItems(pageable);
        return ResponseEntity.ok(orderItems);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderItemResponse> createOrderItem(
            @Valid @RequestBody CreateOrderItemRequest createOrderItemRequest) {
        controllerLogger.methodCalled("OrderItemController", "createOrderItem", createOrderItemRequest.getItemId(), createOrderItemRequest.getQuantity());
        OrderItemResponse createdOrderItem = orderItemService.createOrderItem(createOrderItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrderItem);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<OrderItemResponse> updateOrderItem(
            @Valid @RequestBody UpdateOrderItemRequest updateOrderItemRequest) {
        controllerLogger.methodCalled("OrderItemController", "updateOrderItem", updateOrderItemRequest.getId());
        OrderItemResponse updatedOrderItem = orderItemService.updateOrderItem(updateOrderItemRequest);
        return ResponseEntity.ok(updatedOrderItem);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> activateOrderItem(@PathVariable UUID id) {
        controllerLogger.methodCalled("OrderItemController", "activateOrderItem", id);
        orderItemService.activateOrderItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateOrderItem(@PathVariable UUID id) {
        controllerLogger.methodCalled("OrderItemController", "deactivateOrderItem", id);
        orderItemService.deactivateOrderItem(id);
        return ResponseEntity.noContent().build();
    }

}
