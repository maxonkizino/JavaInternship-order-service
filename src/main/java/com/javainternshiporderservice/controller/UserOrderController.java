package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.logging.ControllerLogger;
import com.javainternshiporderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/orders")
@RequiredArgsConstructor
public class UserOrderController {

    private final OrderService orderService;
    private final ControllerLogger controllerLogger;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Page<OrderWithUserResponse>> getOrdersForUser(
            @PathVariable Long userId,
            Pageable pageable) {
        controllerLogger.methodCalled("UserOrderController", "getOrdersForUser", userId);
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId, pageable));
    }
}
