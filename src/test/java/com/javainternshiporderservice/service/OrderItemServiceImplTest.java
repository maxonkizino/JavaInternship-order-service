package com.javainternshiporderservice.service;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import com.javainternshiporderservice.exception.OrderItemNotFoundException;
import com.javainternshiporderservice.mapper.OrderItemMapper;
import com.javainternshiporderservice.model.OrderItem;
import com.javainternshiporderservice.repository.OrderItemRepository;
import com.javainternshiporderservice.service.impl.OrderItemServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private UUID orderItemId;
    private UUID orderId;
    private UUID itemId;
    private OrderItem orderItem;
    private OrderItemResponse orderItemResponse;

    @BeforeEach
    void setUp() {
        orderItemId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        itemId = UUID.randomUUID();

        orderItem = new OrderItem();
        orderItem.setId(orderItemId);
        orderItem.setQuantity(5);
        orderItem.setActive(true);

        orderItemResponse = new OrderItemResponse();
        orderItemResponse.setId(orderItemId);
        orderItemResponse.setQuantity(5);
    }

    @Test
    void getOrderItemById_shouldReturnOrderItem_whenExists() {
        // given
        when(orderItemRepository.findOne(any(Specification.class))).thenReturn(Optional.of(orderItem));
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        // when
        OrderItemResponse result = orderItemService.getOrderItemById(orderItemId);

        // then
        assertThat(result).isEqualTo(orderItemResponse);
        verify(orderItemRepository).findOne(any(Specification.class));
    }

    @Test
    void getOrderItemById_shouldThrowException_whenNotFound() {
        // given
        when(orderItemRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderItemService.getOrderItemById(orderItemId))
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessageContaining("OrderItem not found with id: " + orderItemId);
    }

    @Test
    void getOrderItemByOrderId_shouldReturnOrderItem_whenExists() {
        // given
        when(orderItemRepository.findOne(any(Specification.class))).thenReturn(Optional.of(orderItem));
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        // when
        OrderItemResponse result = orderItemService.getOrderItemByOrderId(orderId);

        // then
        assertThat(result).isEqualTo(orderItemResponse);
    }

    @Test
    void getOrderItemByItemId_shouldReturnOrderItem_whenExists() {
        // given
        when(orderItemRepository.findOne(any(Specification.class))).thenReturn(Optional.of(orderItem));
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        // when
        OrderItemResponse result = orderItemService.getOrderItemByItemId(itemId);

        // then
        assertThat(result).isEqualTo(orderItemResponse);
    }

    @Test
    void getAllOrderItems_shouldReturnPageOfOrderItems() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItem> orderItemPage = new PageImpl<>(Collections.singletonList(orderItem));

        when(orderItemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderItemPage);
        when(orderItemMapper.toOrderItemResponsesPage(orderItemPage)).thenReturn(new PageImpl<>(Collections.singletonList(orderItemResponse)));

        // when
        Page<OrderItemResponse> result = orderItemService.getAllOrderItems(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(orderItemResponse);
    }

    @Test
    void createOrderItem_shouldCreateAndReturnOrderItem() {
        // given
        CreateOrderItemRequest request = new CreateOrderItemRequest();
        request.setQuantity(10);

        when(orderItemMapper.toOrderItem(request)).thenReturn(orderItem);
        when(orderItemRepository.save(orderItem)).thenReturn(orderItem);
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        // when
        OrderItemResponse result = orderItemService.createOrderItem(request);

        // then
        assertThat(result).isEqualTo(orderItemResponse);
        verify(orderItemRepository).save(orderItem);
    }

    @Test
    void updateOrderItem_shouldUpdateAndReturnOrderItem() {
        // given
        UpdateOrderItemRequest request = new UpdateOrderItemRequest();
        request.setId(orderItemId);
        request.setQuantity(20);

        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(orderItem)).thenReturn(orderItem);
        when(orderItemMapper.toOrderItemResponse(orderItem)).thenReturn(orderItemResponse);

        // when
        OrderItemResponse result = orderItemService.updateOrderItem(request);

        // then
        assertThat(result).isEqualTo(orderItemResponse);
        verify(orderItemMapper).updateOrderItem(request, orderItem);
        verify(orderItemRepository).save(orderItem);
    }

    @Test
    void updateOrderItem_shouldThrowException_whenNotFound() {
        // given
        UpdateOrderItemRequest request = new UpdateOrderItemRequest();
        request.setId(orderItemId);

        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderItemService.updateOrderItem(request))
                .isInstanceOf(OrderItemNotFoundException.class)
                .hasMessageContaining("OrderItem not found with id: " + orderItemId);
    }

    @Test
    void activateOrderItem_shouldCallRepositoryActivate() {
        // given
        doNothing().when(orderItemRepository).activateOrderItem(orderItemId);

        // when
        orderItemService.activateOrderItem(orderItemId);

        // then
        verify(orderItemRepository).activateOrderItem(orderItemId);
    }

    @Test
    void deactivateOrderItem_shouldCallRepositoryDeactivate() {
        // given
        doNothing().when(orderItemRepository).deactivateOrderItem(orderItemId);

        // when
        orderItemService.deactivateOrderItem(orderItemId);

        // then
        verify(orderItemRepository).deactivateOrderItem(orderItemId);
    }

    @Test
    void getOrderItemsWithFilter_shouldApplyActiveFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItem> orderItemPage = new PageImpl<>(Collections.singletonList(orderItem));

        when(orderItemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderItemPage);
        when(orderItemMapper.toOrderItemResponsesPage(orderItemPage)).thenReturn(new PageImpl<>(Collections.singletonList(orderItemResponse)));

        // when
        Page<OrderItemResponse> result = orderItemService.getOrderItemsWithFilter(false, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getOrderItemsWithFilter_shouldApplyOrderIdFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItem> orderItemPage = new PageImpl<>(Collections.singletonList(orderItem));

        when(orderItemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderItemPage);
        when(orderItemMapper.toOrderItemResponsesPage(orderItemPage)).thenReturn(new PageImpl<>(Collections.singletonList(orderItemResponse)));

        // when
        Page<OrderItemResponse> result = orderItemService.getOrderItemsWithFilter(null, orderId, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getOrderItemsWithFilter_shouldApplyItemIdFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderItem> orderItemPage = new PageImpl<>(Collections.singletonList(orderItem));

        when(orderItemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderItemPage);
        when(orderItemMapper.toOrderItemResponsesPage(orderItemPage)).thenReturn(new PageImpl<>(Collections.singletonList(orderItemResponse)));

        // when
        Page<OrderItemResponse> result = orderItemService.getOrderItemsWithFilter(null, null, itemId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }
}
