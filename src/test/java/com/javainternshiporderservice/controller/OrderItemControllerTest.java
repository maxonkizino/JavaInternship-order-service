package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import com.javainternshiporderservice.logging.ControllerLogger;
import com.javainternshiporderservice.service.OrderItemService;
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

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemControllerTest {

    @Mock
    private OrderItemService orderItemService;

    @Mock
    private ControllerLogger controllerLogger;

    @InjectMocks
    private OrderItemController orderItemController;

    private UUID orderItemId;
    private UUID orderId;
    private UUID itemId;
    private OrderItemResponse orderItemResponse;
    private CreateOrderItemRequest createOrderItemRequest;
    private UpdateOrderItemRequest updateOrderItemRequest;

    @BeforeEach
    void setUp() {
        orderItemId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        orderItemResponse = new OrderItemResponse();
        orderItemResponse.setId(orderItemId);
        orderItemResponse.setOrderId(orderId);
        orderItemResponse.setItemId(itemId);
        orderItemResponse.setQuantity(5);
        orderItemResponse.setActive(true);

        createOrderItemRequest = new CreateOrderItemRequest();
        createOrderItemRequest.setItemId(itemId);
        createOrderItemRequest.setQuantity(3);

        updateOrderItemRequest = new UpdateOrderItemRequest();
        updateOrderItemRequest.setId(orderItemId);
        updateOrderItemRequest.setQuantity(10);
    }

    @Test
    void getOrderItemById_shouldReturnOrderItem() {
        when(orderItemService.getOrderItemById(orderItemId)).thenReturn(orderItemResponse);

        ResponseEntity<OrderItemResponse> response = orderItemController.getOrderItemById(orderItemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderItemResponse);
        verify(orderItemService).getOrderItemById(orderItemId);
    }

    @Test
    void getAllOrderItems_shouldReturnPageOfOrderItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItemResponse> page = new PageImpl<>(Collections.singletonList(orderItemResponse));
        when(orderItemService.getAllOrderItems(pageable)).thenReturn(page);

        ResponseEntity<Page<OrderItemResponse>> response = orderItemController.getAllOrderItems(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(orderItemService).getAllOrderItems(pageable);
    }

    @Test
    void getOrderItemsWithFilter_shouldReturnFilteredOrderItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItemResponse> page = new PageImpl<>(Collections.singletonList(orderItemResponse));
        when(orderItemService.getOrderItemsWithFilter(true, orderId, itemId, pageable)).thenReturn(page);

        ResponseEntity<Page<OrderItemResponse>> response = orderItemController.getOrderItemsWithFilter(true, orderId, itemId, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(orderItemService).getOrderItemsWithFilter(true, orderId, itemId, pageable);
    }

    @Test
    void getOrderItemsWithFilter_shouldWorkWithNullParameters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItemResponse> page = new PageImpl<>(Collections.singletonList(orderItemResponse));
        when(orderItemService.getOrderItemsWithFilter(null, null, null, pageable)).thenReturn(page);

        ResponseEntity<Page<OrderItemResponse>> response = orderItemController.getOrderItemsWithFilter(null, null, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orderItemService).getOrderItemsWithFilter(null, null, null, pageable);
    }

    @Test
    void createOrderItem_shouldCreateAndReturnOrderItem() {
        when(orderItemService.createOrderItem(createOrderItemRequest)).thenReturn(orderItemResponse);

        ResponseEntity<OrderItemResponse> response = orderItemController.createOrderItem(createOrderItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(orderItemResponse);
        verify(orderItemService).createOrderItem(createOrderItemRequest);
    }

    @Test
    void updateOrderItem_shouldUpdateAndReturnOrderItem() {
        when(orderItemService.updateOrderItem(updateOrderItemRequest)).thenReturn(orderItemResponse);

        ResponseEntity<OrderItemResponse> response = orderItemController.updateOrderItem(updateOrderItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(orderItemResponse);
        verify(orderItemService).updateOrderItem(updateOrderItemRequest);
    }

    @Test
    void activateOrderItem_shouldActivateOrderItem() {
        doNothing().when(orderItemService).activateOrderItem(orderItemId);

        ResponseEntity<Void> response = orderItemController.activateOrderItem(orderItemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(orderItemService).activateOrderItem(orderItemId);
    }

    @Test
    void deactivateOrderItem_shouldDeactivateOrderItem() {
        doNothing().when(orderItemService).deactivateOrderItem(orderItemId);

        ResponseEntity<Void> response = orderItemController.deactivateOrderItem(orderItemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(orderItemService).deactivateOrderItem(orderItemId);
    }

    @Test
    void getAllOrderItems_shouldReturnEmptyPage_whenNoItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItemResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(orderItemService.getAllOrderItems(pageable)).thenReturn(emptyPage);

        ResponseEntity<Page<OrderItemResponse>> response = orderItemController.getAllOrderItems(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEmpty();
        verify(orderItemService).getAllOrderItems(pageable);
    }

    @Test
    void getOrderItemsWithFilter_shouldHandlePartialFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItemResponse> page = new PageImpl<>(Collections.singletonList(orderItemResponse));
        when(orderItemService.getOrderItemsWithFilter(null, orderId, null, pageable)).thenReturn(page);

        ResponseEntity<Page<OrderItemResponse>> response = 
                orderItemController.getOrderItemsWithFilter(null, orderId, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(orderItemService).getOrderItemsWithFilter(null, orderId, null, pageable);
    }

    @Test
    void createOrderItem_shouldHandleLargeQuantity() {
        createOrderItemRequest.setQuantity(1000);
        orderItemResponse.setQuantity(1000);
        when(orderItemService.createOrderItem(createOrderItemRequest)).thenReturn(orderItemResponse);

        ResponseEntity<OrderItemResponse> response = orderItemController.createOrderItem(createOrderItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getQuantity()).isEqualTo(1000);
    }

    @Test
    void updateOrderItem_shouldHandleZeroQuantity() {
        updateOrderItemRequest.setQuantity(0);
        orderItemResponse.setQuantity(0);
        when(orderItemService.updateOrderItem(updateOrderItemRequest)).thenReturn(orderItemResponse);

        ResponseEntity<OrderItemResponse> response = orderItemController.updateOrderItem(updateOrderItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getQuantity()).isZero();
    }
}
