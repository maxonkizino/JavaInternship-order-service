package com.javainternshiporderservice.mapper;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderResponse;
import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.model.OrderItem;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderMapperTest {

    private OrderMapperImpl orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapperImpl();
        try {
            Field field = OrderMapperImpl.class.getDeclaredField("orderItemMapper");
            field.setAccessible(true);
            field.set(orderMapper, orderItemMapper);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to inject OrderItemMapper into OrderMapperImpl", e);
        }

        when(orderItemMapper.toOrderItemResponses(any())).thenReturn(Collections.emptyList());
        when(orderItemMapper.toOrderItem(any(CreateOrderItemRequest.class))).thenAnswer(invocation -> {
            CreateOrderItemRequest request = invocation.getArgument(0);
            OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(request.getQuantity() == null ? 0 : request.getQuantity());
            return orderItem;
        });
    }

    @Test
    void toOrderResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setUserId(1L);
        order.setStatus("PENDING");
        order.setTotalPrice(new BigDecimal("150.00"));
        order.setDeleted(false);

        OrderResponse response = orderMapper.toOrderResponse(order);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getTotalPrice()).isEqualTo(new BigDecimal("150.00"));
        assertThat(response.isDeleted()).isFalse();
    }

    @Test
    void toOrderResponse_shouldReturnNullForNullOrder() {
        OrderResponse response = orderMapper.toOrderResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toOrder_shouldMapAllFields() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(2L);
        request.setStatus("CONFIRMED");
        request.setTotalPrice(new BigDecimal("200.00"));
        request.setDeleted(false);

        CreateOrderItemRequest itemRequest = new CreateOrderItemRequest();
        itemRequest.setItemId(UUID.randomUUID());
        itemRequest.setQuantity(3);
        request.setOrderItems(Collections.singletonList(itemRequest));

        Order order = orderMapper.toOrder(request);

        assertThat(order).isNotNull();
        assertThat(order.getUserId()).isEqualTo(2L);
        assertThat(order.getStatus()).isEqualTo("CONFIRMED");
        assertThat(order.getTotalPrice()).isEqualTo(new BigDecimal("200.00"));
        assertThat(order.isDeleted()).isFalse();
        assertThat(order.getOrderItems()).hasSize(1);
    }

    @Test
    void toOrder_shouldReturnNullForNullRequest() {
        Order order = orderMapper.toOrder(null);
        assertThat(order).isNull();
    }

    @Test
    void updateOrder_shouldUpdateNonNullFields() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setUserId(1L);
        order.setStatus("PENDING");
        order.setTotalPrice(new BigDecimal("100.00"));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setId(id);
        request.setUserId(2L);
        request.setStatus("CONFIRMED");
        request.setTotalPrice(new BigDecimal("150.00"));

        orderMapper.updateOrder(request, order);

        assertThat(order.getUserId()).isEqualTo(2L);
        assertThat(order.getStatus()).isEqualTo("CONFIRMED");
        assertThat(order.getTotalPrice()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void updateOrder_shouldIgnoreNullFields() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setUserId(1L);
        order.setStatus("PENDING");
        order.setTotalPrice(new BigDecimal("100.00"));

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setId(id);
        request.setStatus("CONFIRMED");

        orderMapper.updateOrder(request, order);

        assertThat(order.getUserId()).isEqualTo(1L);
        assertThat(order.getStatus()).isEqualTo("CONFIRMED");
        assertThat(order.getTotalPrice()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void toOrderResponses_shouldMapList() {
        Order order1 = new Order();
        order1.setId(UUID.randomUUID());
        order1.setUserId(1L);
        order1.setStatus("PENDING");

        Order order2 = new Order();
        order2.setId(UUID.randomUUID());
        order2.setUserId(2L);
        order2.setStatus("CONFIRMED");

        List<Order> orders = Arrays.asList(order1, order2);
        List<OrderResponse> responses = orderMapper.toOrderResponses(orders);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getStatus()).isEqualTo("PENDING");
        assertThat(responses.get(1).getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void toOrderResponses_shouldReturnNullForNullList() {
        List<OrderResponse> responses = orderMapper.toOrderResponses(null);
        assertThat(responses).isNull();
    }

    @Test
    void toOrderResponses_shouldReturnEmptyListForEmptyInput() {
        List<OrderResponse> responses = orderMapper.toOrderResponses(Collections.emptyList());
        assertThat(responses).isEmpty();
    }

    @Test
    void toOrderResponsesPage_shouldMapPage() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(1L);
        order.setStatus("PENDING");

        Page<Order> orderPage = new PageImpl<>(
            Collections.singletonList(order),
            PageRequest.of(0, 10),
            1
        );

        Page<OrderResponse> responsePage = orderMapper.toOrderResponsesPage(orderPage);

        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getContent().get(0).getStatus()).isEqualTo("PENDING");
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
    }

    @Test
    void toOrderResponsesPage_shouldReturnNullForNullPage() {
        Page<OrderResponse> responsePage = orderMapper.toOrderResponsesPage(null);
        assertThat(responsePage).isNull();
    }

    @Test
    void toOrderResponsesPage_shouldHandleMultipleItems() {
        Order order1 = new Order();
        order1.setId(UUID.randomUUID());
        order1.setUserId(1L);

        Order order2 = new Order();
        order2.setId(UUID.randomUUID());
        order2.setUserId(2L);

        Order order3 = new Order();
        order3.setId(UUID.randomUUID());
        order3.setUserId(3L);

        Page<Order> orderPage = new PageImpl<>(
            Arrays.asList(order1, order2, order3),
            PageRequest.of(1, 5),
            3
        );

        Page<OrderResponse> responsePage = orderMapper.toOrderResponsesPage(orderPage);

        assertThat(responsePage.getContent()).hasSize(3);
        assertThat(responsePage.getNumber()).isEqualTo(1);
    }

    @Test
    void updateOrder_shouldHandleOrderItems() {
        UUID id = UUID.randomUUID();
        Order order = new Order();
        order.setId(id);
        order.setUserId(1L);
        order.setStatus("PENDING");

        OrderItem existingItem = new OrderItem();
        existingItem.setId(UUID.randomUUID());
        existingItem.setQuantity(1);
        order.setOrderItems(new java.util.ArrayList<>(Collections.singletonList(existingItem)));

        UpdateOrderItemRequest updateItemRequest = new UpdateOrderItemRequest();
        updateItemRequest.setId(UUID.randomUUID());
        updateItemRequest.setQuantity(5);

        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setId(id);
        request.setStatus("CONFIRMED");
        request.setOrderItems(Collections.singletonList(updateItemRequest));

        orderMapper.updateOrder(request, order);

        assertThat(order.getStatus()).isEqualTo("CONFIRMED");
    }
}
