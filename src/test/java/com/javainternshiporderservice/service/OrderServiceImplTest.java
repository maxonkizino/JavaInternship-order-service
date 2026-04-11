package com.javainternshiporderservice.service;

import com.javainternshiporderservice.client.UserServiceClient;
import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.dto.response.UserInfoResponse;
import com.javainternshiporderservice.exception.OrderNotFoundException;
import com.javainternshiporderservice.mapper.OrderMapper;
import com.javainternshiporderservice.mapper.OrderWithUserAssembler;
import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.repository.OrderItemRepository;
import com.javainternshiporderservice.repository.OrderRepository;
import com.javainternshiporderservice.security.SecurityUtils;
import com.javainternshiporderservice.service.impl.OrderServiceImpl;
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
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderWithUserAssembler orderWithUserAssembler;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID orderId;
    private Long userId;
    private Order order;
    private OrderWithUserResponse orderWithUserResponse;
    private UserInfoResponse userInfoResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        userId = 1L;
        order = new Order();
        order.setId(orderId);
        order.setUserId(userId);
        order.setStatus("PENDING");
        order.setTotalPrice(new BigDecimal("100.00"));
        order.setDeleted(false);

        userInfoResponse = new UserInfoResponse();
        userInfoResponse.setId(userId);
        userInfoResponse.setName("John");
        userInfoResponse.setSurname("Doe");

        orderWithUserResponse = new OrderWithUserResponse();
    }

    @Test
    void getOrderById_shouldReturnOrder_whenUserIsOwner() {
        // given
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        OrderWithUserResponse result = orderService.getOrderById(orderId);

        // then
        assertThat(result).isEqualTo(orderWithUserResponse);
        verify(orderRepository).findOne(any(Specification.class));
        verify(securityUtils).isOwnerOrAdmin(userId);
    }

    @Test
    void getOrderById_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // given
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to order");
    }

    @Test
    void getOrderById_shouldThrowOrderNotFoundException_whenOrderNotFound() {
        // given
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: " + orderId);
    }

    @Test
    void getOrderByUserId_shouldReturnOrder_whenUserIsOwner() {
        // given
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        OrderWithUserResponse result = orderService.getOrderByUserId(userId);

        // then
        assertThat(result).isEqualTo(orderWithUserResponse);
    }

    @Test
    void getOrderByUserId_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // given
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.getOrderByUserId(userId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to orders for user");
    }

    @Test
    void getAllOrders_shouldReturnAllOrders_whenUserIsAdmin() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));

        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        Page<OrderWithUserResponse> result = orderService.getAllOrders(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(orderWithUserResponse);
    }

    @Test
    void getAllOrders_shouldReturnOnlyUserOrders_whenUserIsNotAdmin() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));

        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        Page<OrderWithUserResponse> result = orderService.getAllOrders(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void createOrder_shouldCreateOrder_whenUserIsAdmin() {
        // given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(userId);
        request.setStatus("PENDING");
        request.setTotalPrice(new BigDecimal("100.00"));

        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(orderMapper.toOrder(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        OrderWithUserResponse result = orderService.createOrder(request);

        // then
        assertThat(result).isEqualTo(orderWithUserResponse);
        verify(orderRepository).save(order);
    }

    @Test
    void createOrder_shouldCreateOrder_whenUserCreatesForHimself() {
        // given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(userId);
        request.setStatus("PENDING");
        request.setTotalPrice(new BigDecimal("100.00"));

        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(orderMapper.toOrder(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        OrderWithUserResponse result = orderService.createOrder(request);

        // then
        assertThat(result).isEqualTo(orderWithUserResponse);
    }

    @Test
    void createOrder_shouldThrowAccessDenied_whenUserCreatesForOthers() {
        // given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(2L); // different user
        request.setStatus("PENDING");
        request.setTotalPrice(new BigDecimal("100.00"));

        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("can only create orders for yourself");
    }

    @Test
    void updateOrder_shouldUpdateOrder_whenUserIsOwner() {
        // given
        UpdateOrderRequest request = new UpdateOrderRequest();
        request.setId(orderId);
        request.setStatus("CONFIRMED");

        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);
        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(orderRepository.save(order)).thenReturn(order);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        OrderWithUserResponse result = orderService.updateOrder(orderId, request);

        // then
        assertThat(result).isEqualTo(orderWithUserResponse);
        verify(orderMapper).updateOrder(request, order);
    }

    @Test
    void activateOrder_shouldCallRepositories_whenUserIsOwner() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);

        orderService.activateOrder(orderId);

        verify(orderRepository).activateOrder(orderId);
        verify(orderItemRepository).activateByOrderId(orderId);
    }

    @Test
    void activateOrder_shouldThrowAccessDenied_whenUserIsNotOwner() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.activateOrder(orderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cannot activate order");
    }

    @Test
    void activateOrder_shouldThrowOrderNotFound_whenOrderMissing() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.activateOrder(orderId))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order not found with id: " + orderId);
    }

    @Test
    void getOrdersWithFilter_shouldReturnSoftDeleted_whenActiveIsFalse() {
        Pageable pageable = PageRequest.of(0, 10);
        Order deleted = new Order();
        deleted.setId(orderId);
        deleted.setUserId(userId);
        deleted.setDeleted(true);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(deleted));

        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(deleted, userInfoResponse)).thenReturn(orderWithUserResponse);

        Page<OrderWithUserResponse> result = orderService.getOrdersWithFilter(
                false, null, null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void deactivateOrder_shouldDeactivate_whenUserIsOwner() {
        // given
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);

        // when
        orderService.deactivateOrder(orderId);

        // then
        verify(orderRepository).deactivateOrder(orderId);
        verify(orderItemRepository).deactivateByOrderId(orderId);
    }

    @Test
    void deactivateOrder_shouldThrowAccessDenied_whenUserIsNotOwner() {
        // given
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> orderService.deactivateOrder(orderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied: cannot deactivate order");
    }

    @Test
    void getOrdersWithFilter_shouldApplyUserFilter_whenUserIsNotAdmin() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));

        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        Page<OrderWithUserResponse> result = orderService.getOrdersWithFilter(
                true, "PENDING", null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getOrdersWithFilter_shouldThrowAccessDenied_whenUserFiltersByOtherUserId() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(securityUtils.isCurrentUserAdmin()).thenReturn(false);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        // when & then
        assertThatThrownBy(() -> orderService.getOrdersWithFilter(
                null, null, null, null, null, 999L, pageable))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("cannot filter by other user ID");
    }
}
