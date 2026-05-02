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
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        userInfoResponse.setEmail("john@example.com");

        orderWithUserResponse = new OrderWithUserResponse();
    }

    @Test
    void getOrderById_shouldReturnOrder_whenUserIsOwner() {
        // given
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(securityUtils.getCurrentUserEmail()).thenReturn("john@example.com");
        when(userServiceClient.fetchUserByEmail("john@example.com")).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        // when
        OrderWithUserResponse result = orderService.getOrderById(orderId);

        // then
        assertThat(result).isEqualTo(orderWithUserResponse);
        verify(orderRepository).findOne(any(Specification.class));
        verify(securityUtils).isOwnerOrAdmin(userId);
        verify(userServiceClient).fetchUserByEmail("john@example.com");
    }

    @Test
    void getOrderById_shouldUseUserIdLookup_whenJwtEmailMissing() {
        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(securityUtils.getCurrentUserEmail()).thenReturn(null);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        OrderWithUserResponse result = orderService.getOrderById(orderId);

        assertThat(result).isEqualTo(orderWithUserResponse);
        verify(userServiceClient).fetchUserById(userId);
        verify(userServiceClient, never()).fetchUserByEmail(any(String.class));
    }

    @Test
    void getOrderById_shouldThrowAccessDenied_whenJwtEmailDoesNotMatchOrderOwner() {
        UserInfoResponse wrongUser = new UserInfoResponse();
        wrongUser.setId(99L);
        wrongUser.setEmail("other@example.com");

        when(orderRepository.findOne(any(Specification.class))).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(securityUtils.getCurrentUserEmail()).thenReturn("other@example.com");
        when(userServiceClient.fetchUserByEmail("other@example.com")).thenReturn(wrongUser);

        assertThatThrownBy(() -> orderService.getOrderById(orderId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Token email does not match order owner");
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
    void getOrdersByUserId_shouldReturnPage_whenUserIsOwner() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));

        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(securityUtils.getCurrentUserEmail()).thenReturn("john@example.com");
        when(userServiceClient.fetchUserByEmail("john@example.com")).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        Page<OrderWithUserResponse> result = orderService.getOrdersByUserId(userId, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(orderWithUserResponse);
    }

    @Test
    void getOrdersByUserId_shouldThrowAccessDenied_whenUserIsNotOwner() {
        Pageable pageable = PageRequest.of(0, 10);
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(false);

        assertThatThrownBy(() -> orderService.getOrdersByUserId(userId, pageable))
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
        when(securityUtils.getCurrentUserEmail()).thenReturn("john@example.com");
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.fetchUserByEmail("john@example.com")).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

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
        when(orderRepository.saveAndFlush(order)).thenReturn(order);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        OrderWithUserResponse result = orderService.createOrder(request);

        // then
        assertThat(result).isEqualTo(orderWithUserResponse);
        verify(orderRepository).saveAndFlush(order);
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
        when(securityUtils.getCurrentUserEmail()).thenReturn("john@example.com");
        when(orderMapper.toOrder(request)).thenReturn(order);
        when(orderRepository.saveAndFlush(order)).thenReturn(order);
        when(userServiceClient.fetchUserByEmail("john@example.com")).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

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
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(securityUtils.getCurrentUserEmail()).thenReturn("john@example.com");
        when(orderRepository.save(order)).thenReturn(order);
        when(userServiceClient.fetchUserByEmail("john@example.com")).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        OrderWithUserResponse result = orderService.updateOrder(orderId, request);

        // then
        assertThat(result).isEqualTo(orderWithUserResponse);
        verify(orderMapper).updateOrder(request, order);
    }

    @Test
    void activateOrder_shouldCallRepositories_whenUserIsOwner() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(securityUtils.isOwnerOrAdmin(userId)).thenReturn(true);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        OrderWithUserResponse result = orderService.activateOrder(orderId);

        assertThat(result).isEqualTo(orderWithUserResponse);
        verify(orderRepository).activateOrder(orderId);
        verify(orderItemRepository).activateByOrderId(orderId);
        verify(orderRepository, times(2)).findById(orderId);
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
        when(securityUtils.getCurrentUserEmail()).thenReturn("john@example.com");
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.fetchUserByEmail("john@example.com")).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        Page<OrderWithUserResponse> result = orderService.getOrdersWithFilter(
                true, "PENDING", null, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getOrdersWithFilter_shouldApplyStatusesList_whenAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));
        List<String> statuses = List.of("PENDING", "CONFIRMED");

        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        Page<OrderWithUserResponse> result = orderService.getOrdersWithFilter(
                true, null, statuses, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getOrdersWithFilter_shouldApplyCreatedAtRange_whenAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Collections.singletonList(order));
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");

        when(securityUtils.isCurrentUserAdmin()).thenReturn(true);
        when(orderRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(orderPage);
        when(userServiceClient.fetchUserById(userId)).thenReturn(userInfoResponse);
        when(orderWithUserAssembler.assemble(order, userInfoResponse)).thenReturn(orderWithUserResponse);

        Page<OrderWithUserResponse> result = orderService.getOrdersWithFilter(
                null, null, null, from, to, null, pageable);

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
