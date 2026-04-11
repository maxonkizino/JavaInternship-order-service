package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<OrderWithUserResponse>> getOrders(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) Instant createdAtFrom,
            @RequestParam(required = false) Instant createdAtTo,
            @RequestParam(required = false) Long userId,
            Pageable pageable) {
        if (hasFilterParams(active, status, statuses, createdAtFrom, createdAtTo, userId)) {
            Page<OrderWithUserResponse> orders = orderService.getOrdersWithFilter(
                    active, status, statuses, createdAtFrom, createdAtTo, userId, pageable);
            return ResponseEntity.ok(orders);
        }
        Page<OrderWithUserResponse> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }

    private static boolean hasFilterParams(
            Boolean active,
            String status,
            List<String> statuses,
            Instant createdAtFrom,
            Instant createdAtTo,
            Long userId) {
        return active != null
                || status != null
                || (statuses != null && !statuses.isEmpty())
                || createdAtFrom != null
                || createdAtTo != null
                || userId != null;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderWithUserResponse> getOrderById(
            @PathVariable UUID id,
            @RequestParam(required = false) @Email String userEmail) {
        if (userEmail != null && !userEmail.isBlank()) {
            return ResponseEntity.ok(orderService.getOrderWithUserById(id, userEmail));
        }
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<OrderWithUserResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest) {
        OrderWithUserResponse createdOrder = orderService.createOrder(createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<OrderWithUserResponse> updateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderRequest updateOrderRequest) {
        OrderWithUserResponse updatedOrder = orderService.updateOrder(id, updateOrderRequest);
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
