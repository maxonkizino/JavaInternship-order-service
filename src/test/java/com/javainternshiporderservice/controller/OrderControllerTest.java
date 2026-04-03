package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private UUID orderId;
    private Long userId;
    private OrderWithUserResponse orderResponse;
    private CreateOrderRequest createOrderRequest;
    private UpdateOrderRequest updateOrderRequest;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = 1L;

        orderResponse = new OrderWithUserResponse();

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setUserId(userId);
        createOrderRequest.setStatus("PENDING");
        createOrderRequest.setTotalPrice(new BigDecimal("150.00"));
        createOrderRequest.setActive(true);

        updateOrderRequest = new UpdateOrderRequest();
        updateOrderRequest.setId(orderId);
        updateOrderRequest.setUserId(userId);
        updateOrderRequest.setStatus("CONFIRMED");
        updateOrderRequest.setTotalPrice(new BigDecimal("200.00"));
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        when(orderService.getOrderById(orderId)).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = orderController.getOrderById(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderResponse);
        verify(orderService).getOrderById(orderId);
    }

    @Test
    void getOrderByUserId_shouldReturnOrder() {
        when(orderService.getOrderByUserId(userId)).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = orderController.getOrderByUserId(userId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderResponse);
        verify(orderService).getOrderByUserId(userId);
    }

    @Test
    void getAllOrders_shouldReturnPageOfOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderWithUserResponse> page = new PageImpl<>(Collections.singletonList(orderResponse));
        when(orderService.getAllOrders(pageable)).thenReturn(page);

        ResponseEntity<Page<OrderWithUserResponse>> response = orderController.getAllOrders(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(orderService).getAllOrders(pageable);
    }

    @Test
    void getOrdersWithFilter_shouldReturnFilteredOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderWithUserResponse> page = new PageImpl<>(Collections.singletonList(orderResponse));
        
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        List<String> statuses = List.of("PENDING", "CONFIRMED");
        
        when(orderService.getOrdersWithFilter(true, "PENDING", statuses, from, to, userId, pageable))
                .thenReturn(page);

        ResponseEntity<Page<OrderWithUserResponse>> response = orderController.getOrdersWithFilter(
                true, "PENDING", statuses, from, to, userId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(orderService).getOrdersWithFilter(true, "PENDING", statuses, from, to, userId, pageable);
    }

    @Test
    void getOrdersWithFilter_shouldWorkWithNullParameters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderWithUserResponse> page = new PageImpl<>(Collections.singletonList(orderResponse));
        
        when(orderService.getOrdersWithFilter(null, null, null, null, null, null, pageable))
                .thenReturn(page);

        ResponseEntity<Page<OrderWithUserResponse>> response = orderController.getOrdersWithFilter(
                null, null, null, null, null, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orderService).getOrdersWithFilter(null, null, null, null, null, null, pageable);
    }

    @Test
    void createOrder_shouldCreateAndReturnOrder() {
        when(orderService.createOrder(createOrderRequest)).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = orderController.createOrder(createOrderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(orderResponse);
        verify(orderService).createOrder(createOrderRequest);
    }

    @Test
    void updateOrder_shouldUpdateAndReturnOrder() {
        when(orderService.updateOrder(updateOrderRequest)).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = orderController.updateOrder(updateOrderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderResponse);
        verify(orderService).updateOrder(updateOrderRequest);
    }

    @Test
    void activateOrder_shouldActivateOrder() {
        doNothing().when(orderService).activateOrder(orderId);

        ResponseEntity<Void> response = orderController.activateOrder(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(orderService).activateOrder(orderId);
    }

    @Test
    void deactivateOrder_shouldDeactivateOrder() {
        doNothing().when(orderService).deactivateOrder(orderId);

        ResponseEntity<Void> response = orderController.deactivateOrder(orderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(orderService).deactivateOrder(orderId);
    }

    @Test
    void getOrderWithUserById_shouldReturnOrderWithUser() {
        when(orderService.getOrderWithUserById(orderId, "user@example.com")).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = 
                orderController.getOrderWithUserById(orderId, "user@example.com");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderResponse);
        verify(orderService).getOrderWithUserById(orderId, "user@example.com");
    }

    @Test
    void getAllOrders_shouldReturnEmptyPage_whenNoOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderWithUserResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(orderService.getAllOrders(pageable)).thenReturn(emptyPage);

        ResponseEntity<Page<OrderWithUserResponse>> response = orderController.getAllOrders(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEmpty();
    }

    @Test
    void getOrdersWithFilter_shouldHandleSingleStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderWithUserResponse> page = new PageImpl<>(Collections.singletonList(orderResponse));
        
        when(orderService.getOrdersWithFilter(true, "PENDING", null, null, null, userId, pageable))
                .thenReturn(page);

        ResponseEntity<Page<OrderWithUserResponse>> response = 
                orderController.getOrdersWithFilter(true, "PENDING", null, null, null, userId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orderService).getOrdersWithFilter(true, "PENDING", null, null, null, userId, pageable);
    }

    @Test
    void createOrder_shouldHandleZeroPrice() {
        createOrderRequest.setTotalPrice(BigDecimal.ZERO);
        when(orderService.createOrder(createOrderRequest)).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = orderController.createOrder(createOrderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(orderResponse);
    }

    @Test
    void updateOrder_shouldHandleDifferentStatus() {
        updateOrderRequest.setStatus("CANCELLED");
        when(orderService.updateOrder(updateOrderRequest)).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = orderController.updateOrder(updateOrderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateOrder_shouldHandlePriceChange() {
        updateOrderRequest.setTotalPrice(new BigDecimal("500.00"));
        when(orderService.updateOrder(updateOrderRequest)).thenReturn(orderResponse);

        ResponseEntity<OrderWithUserResponse> response = orderController.updateOrder(updateOrderRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
