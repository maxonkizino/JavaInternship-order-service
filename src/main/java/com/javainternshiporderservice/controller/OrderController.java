package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderWithUserResponse> getOrderById(@PathVariable UUID id) {
        OrderWithUserResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{id}/with-user")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderWithUserResponse> getOrderWithUserById(
            @PathVariable UUID id,
            @RequestParam @NotNull @Email String userEmail) {
        OrderWithUserResponse response = orderService.getOrderWithUserById(id, userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderWithUserResponse> getOrderByUserId(@PathVariable Long userId) {
        OrderWithUserResponse order = orderService.getOrderByUserId(userId);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<OrderWithUserResponse>> getAllOrders(Pageable pageable) {
        Page<OrderWithUserResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderWithUserResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest) {
        OrderWithUserResponse createdOrder = orderService.createOrder(createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<OrderWithUserResponse> updateOrder(
            @Valid @RequestBody UpdateOrderRequest updateOrderRequest) {
        OrderWithUserResponse updatedOrder = orderService.updateOrder(updateOrderRequest);
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> activateOrder(@PathVariable UUID id) {
        orderService.activateOrder(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deactivateOrder(@PathVariable UUID id) {
        orderService.deactivateOrder(id);
        return ResponseEntity.noContent().build();
    }

}
