package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Validated
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderItemResponse> getOrderItemById(@PathVariable UUID id) {
        OrderItemResponse orderItem = orderItemService.getOrderItemById(id);
        return ResponseEntity.ok(orderItem);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<OrderItemResponse>> getAllOrderItems(Pageable pageable) {
        Page<OrderItemResponse> orderItems = orderItemService.getAllOrderItems(pageable);
        return ResponseEntity.ok(orderItems);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderItemResponse> createOrderItem(
            @Valid @RequestBody CreateOrderItemRequest createOrderItemRequest) {
        OrderItemResponse createdOrderItem = orderItemService.createOrderItem(createOrderItemRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrderItem);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<OrderItemResponse> updateOrderItem(
            @Valid @RequestBody UpdateOrderItemRequest updateOrderItemRequest) {
        OrderItemResponse updatedOrderItem = orderItemService.updateOrderItem(updateOrderItemRequest);
        return ResponseEntity.ok(updatedOrderItem);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> activateOrderItem(@PathVariable UUID id) {
        orderItemService.activateOrderItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateOrderItem(@PathVariable UUID id) {
        orderItemService.deactivateOrderItem(id);
        return ResponseEntity.noContent().build();
    }

}
