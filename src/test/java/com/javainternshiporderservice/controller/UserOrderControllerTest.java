package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserOrderControllerTest {

    @Mock
    private OrderService orderService;

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
    void getOrderForUser_shouldReturnOrder() {
        when(orderService.getOrderByUserId(userId)).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = userOrderController.getOrderForUser(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderResponse);
        verify(orderService).getOrderByUserId(userId);
    }
}
