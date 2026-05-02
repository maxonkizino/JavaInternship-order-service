package com.javainternshiporderservice.mapper;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import com.javainternshiporderservice.model.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemMapperTest {

    private OrderItemMapperImpl orderItemMapper;

    @BeforeEach
    void setUp() {
        orderItemMapper = new OrderItemMapperImpl();
    }

    @Test
    void toOrderItemResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();

        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setQuantity(5);
        orderItem.setActive(true);

        OrderItemResponse response = orderItemMapper.toOrderItemResponse(orderItem);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getQuantity()).isEqualTo(5);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void toOrderItemResponse_shouldReturnNullForNullOrderItem() {
        OrderItemResponse response = orderItemMapper.toOrderItemResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toOrderItem_shouldMapAllFields() {
        UUID itemId = UUID.randomUUID();
        CreateOrderItemRequest request = new CreateOrderItemRequest();
        request.setItemId(itemId);
        request.setQuantity(3);
        request.setActive(true);

        OrderItem orderItem = orderItemMapper.toOrderItem(request);

        assertThat(orderItem).isNotNull();
        assertThat(orderItem.getQuantity()).isEqualTo(3);
        assertThat(orderItem.isActive()).isTrue();
    }

    @Test
    void toOrderItem_shouldReturnNullForNullRequest() {
        OrderItem orderItem = orderItemMapper.toOrderItem(null);
        assertThat(orderItem).isNull();
    }

    @Test
    void updateOrderItem_shouldUpdateNonNullFields() {
        UUID id = UUID.randomUUID();
        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setQuantity(2);
        orderItem.setActive(false);

        UpdateOrderItemRequest request = new UpdateOrderItemRequest();
        request.setId(id);
        request.setQuantity(10);

        orderItemMapper.updateOrderItem(request, orderItem);

        assertThat(orderItem.getQuantity()).isEqualTo(10);
        assertThat(orderItem.isActive()).isFalse();
    }

    @Test
    void updateOrderItem_shouldIgnoreNullFields() {
        UUID id = UUID.randomUUID();
        OrderItem orderItem = new OrderItem();
        orderItem.setId(id);
        orderItem.setQuantity(5);

        UpdateOrderItemRequest request = new UpdateOrderItemRequest();
        request.setId(id);

        orderItemMapper.updateOrderItem(request, orderItem);

        assertThat(orderItem.getQuantity()).isEqualTo(5);
    }

    @Test
    void toOrderItemResponses_shouldMapList() {
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(UUID.randomUUID());
        orderItem1.setQuantity(1);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(UUID.randomUUID());
        orderItem2.setQuantity(2);

        List<OrderItem> orderItems = Arrays.asList(orderItem1, orderItem2);
        List<OrderItemResponse> responses = orderItemMapper.toOrderItemResponses(orderItems);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getQuantity()).isEqualTo(1);
        assertThat(responses.get(1).getQuantity()).isEqualTo(2);
    }

    @Test
    void toOrderItemResponses_shouldReturnNullForNullList() {
        List<OrderItemResponse> responses = orderItemMapper.toOrderItemResponses(null);
        assertThat(responses).isNull();
    }

    @Test
    void toOrderItemResponses_shouldReturnEmptyListForEmptyInput() {
        List<OrderItemResponse> responses = orderItemMapper.toOrderItemResponses(Collections.emptyList());
        assertThat(responses).isEmpty();
    }

    @Test
    void toOrderItemResponsesPage_shouldMapPage() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setQuantity(7);

        Page<OrderItem> itemPage = new PageImpl<>(
            Collections.singletonList(orderItem),
            PageRequest.of(0, 10),
            1
        );

        Page<OrderItemResponse> responsePage = orderItemMapper.toOrderItemResponsesPage(itemPage);

        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getContent().get(0).getQuantity()).isEqualTo(7);
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void toOrderItemResponsesPage_shouldReturnNullForNullPage() {
        Page<OrderItemResponse> responsePage = orderItemMapper.toOrderItemResponsesPage(null);
        assertThat(responsePage).isNull();
    }

    @Test
    void toOrderItemResponsesPage_shouldHandleMultipleItems() {
        OrderItem orderItem1 = new OrderItem();
        orderItem1.setId(UUID.randomUUID());
        orderItem1.setQuantity(1);

        OrderItem orderItem2 = new OrderItem();
        orderItem2.setId(UUID.randomUUID());
        orderItem2.setQuantity(2);

        OrderItem orderItem3 = new OrderItem();
        orderItem3.setId(UUID.randomUUID());
        orderItem3.setQuantity(3);

        Page<OrderItem> itemPage = new PageImpl<>(
            Arrays.asList(orderItem1, orderItem2, orderItem3),
            PageRequest.of(0, 5),
            3
        );

        Page<OrderItemResponse> responsePage = orderItemMapper.toOrderItemResponsesPage(itemPage);

        assertThat(responsePage.getContent()).hasSize(3);
        assertThat(responsePage.getPageable().getPageSize()).isEqualTo(5);
        assertThat(responsePage.getTotalElements()).isEqualTo(3);
    }
}
