package com.javainternshiporderservice.mapper;

import com.javainternshiporderservice.dto.response.OrderResponse;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.dto.response.UserInfoResponse;
import com.javainternshiporderservice.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderWithUserAssemblerTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderWithUserAssembler orderWithUserAssembler;

    private Order order;
    private OrderResponse orderResponse;
    private UserInfoResponse userInfo;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(UUID.randomUUID());
        order.setUserId(1L);
        order.setStatus("PENDING");
        order.setTotalPrice(new BigDecimal("100.00"));

        orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setUserId(1L);
        orderResponse.setStatus("PENDING");
        orderResponse.setTotalPrice(new BigDecimal("100.00"));

        userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);
    }

    @Test
    void assemble_shouldCombineOrderAndUser() {
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        OrderWithUserResponse result = orderWithUserAssembler.assemble(order, userInfo);

        assertThat(result).isNotNull();
        assertThat(result.getOrder()).isEqualTo(orderResponse);
        assertThat(result.getUser()).isEqualTo(userInfo);
    }

    @Test
    void assemble_shouldHandleNullOrder() {
        when(orderMapper.toOrderResponse(null)).thenReturn(null);

        OrderWithUserResponse result = orderWithUserAssembler.assemble(null, userInfo);

        assertThat(result).isNotNull();
        assertThat(result.getOrder()).isNull();
        assertThat(result.getUser()).isEqualTo(userInfo);
    }

    @Test
    void assemble_shouldHandleNullUser() {
        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        OrderWithUserResponse result = orderWithUserAssembler.assemble(order, null);

        assertThat(result).isNotNull();
        assertThat(result.getOrder()).isEqualTo(orderResponse);
        assertThat(result.getUser()).isNull();
    }

    @Test
    void assemble_shouldHandleBothNull() {
        when(orderMapper.toOrderResponse(null)).thenReturn(null);

        OrderWithUserResponse result = orderWithUserAssembler.assemble(null, null);

        assertThat(result).isNotNull();
        assertThat(result.getOrder()).isNull();
        assertThat(result.getUser()).isNull();
    }

    @Test
    void assemble_shouldPreserveAllOrderFields() {
        UUID orderId = UUID.randomUUID();
        OrderResponse fullOrderResponse = new OrderResponse();
        fullOrderResponse.setId(orderId);
        fullOrderResponse.setUserId(5L);
        fullOrderResponse.setStatus("CONFIRMED");
        fullOrderResponse.setTotalPrice(new BigDecimal("250.00"));
        fullOrderResponse.setActive(true);

        Order fullOrder = new Order();
        fullOrder.setId(orderId);
        fullOrder.setUserId(5L);
        fullOrder.setStatus("CONFIRMED");
        fullOrder.setTotalPrice(new BigDecimal("250.00"));
        fullOrder.setActive(true);

        when(orderMapper.toOrderResponse(fullOrder)).thenReturn(fullOrderResponse);

        OrderWithUserResponse result = orderWithUserAssembler.assemble(fullOrder, userInfo);

        assertThat(result.getOrder().getId()).isEqualTo(orderId);
        assertThat(result.getOrder().getUserId()).isEqualTo(5L);
        assertThat(result.getOrder().getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getOrder().getTotalPrice()).isEqualTo(new BigDecimal("250.00"));
        assertThat(result.getOrder().isActive()).isTrue();
    }

    @Test
    void assemble_shouldPreserveAllUserFields() {
        UserInfoResponse fullUserInfo = new UserInfoResponse();
        fullUserInfo.setId(10L);
        fullUserInfo.setName("Jane");
        fullUserInfo.setSurname("Smith");
        fullUserInfo.setEmail("jane.smith@example.com");
        fullUserInfo.setBirthDate(LocalDate.of(1985, 5, 15));
        fullUserInfo.setActive(false);

        when(orderMapper.toOrderResponse(order)).thenReturn(orderResponse);

        OrderWithUserResponse result = orderWithUserAssembler.assemble(order, fullUserInfo);

        assertThat(result.getUser().getId()).isEqualTo(10L);
        assertThat(result.getUser().getName()).isEqualTo("Jane");
        assertThat(result.getUser().getSurname()).isEqualTo("Smith");
        assertThat(result.getUser().getEmail()).isEqualTo("jane.smith@example.com");
        assertThat(result.getUser().getBirthDate()).isEqualTo(LocalDate.of(1985, 5, 15));
        assertThat(result.getUser().isActive()).isFalse();
    }
}
