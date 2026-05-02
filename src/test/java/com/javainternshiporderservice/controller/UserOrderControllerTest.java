package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.logging.ControllerLogger;
import com.javainternshiporderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ControllerLogger controllerLogger;

    @InjectMocks
    private UserOrderController userOrderController;

    private Long userId;
    private OrderWithUserResponse orderResponse;

    @BeforeEach
    void setUp() {
        userId = 1L;
        orderResponse = new OrderWithUserResponse();
    }

    @Test
    void getOrdersForUser_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<OrderWithUserResponse> page = new PageImpl<>(List.of(orderResponse), pageable, 1);
        when(orderService.getOrdersByUserId(userId, pageable)).thenReturn(page);

        ResponseEntity<Page<OrderWithUserResponse>> response =
                userOrderController.getOrdersForUser(userId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(orderService).getOrdersByUserId(userId, pageable);
    }

    @Test
    void getOrdersForUser_shouldUsePathUserId() {
        Long otherUserId = 42L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderWithUserResponse> page = new PageImpl<>(List.of(orderResponse), pageable, 1);
        when(orderService.getOrdersByUserId(otherUserId, pageable)).thenReturn(page);

        ResponseEntity<Page<OrderWithUserResponse>> response =
                userOrderController.getOrdersForUser(otherUserId, pageable);

        assertThat(response.getBody()).isEqualTo(page);
        verify(orderService).getOrdersByUserId(otherUserId, pageable);
        verify(orderService, never()).getOrdersByUserId(eq(userId), any(Pageable.class));
    }
}
