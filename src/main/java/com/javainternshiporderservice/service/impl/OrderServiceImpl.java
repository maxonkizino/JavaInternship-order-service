package com.javainternshiporderservice.service.impl;

import com.javainternshiporderservice.client.UserServiceClient;
import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.dto.response.UserInfoResponse;
import com.javainternshiporderservice.exception.OrderNotFoundException;
import com.javainternshiporderservice.mapper.OrderMapper;
import com.javainternshiporderservice.mapper.OrderWithUserAssembler;
import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.model.OrderItem;
import com.javainternshiporderservice.model.specification.OrderSpecification;
import com.javainternshiporderservice.repository.ItemRepository;
import com.javainternshiporderservice.repository.OrderRepository;
import com.javainternshiporderservice.security.SecurityUtils;
import com.javainternshiporderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found";
    private static final String WITH_ID = " with id: ";
    private static final String ACCESS_DENIED_TO_ORDER = "Access denied to order: ";
    private static final String ACCESS_DENIED_TO_ORDERS_FOR_USER = "Access denied to orders for user: ";
    private static final String ACCESS_DENIED_FILTER_BY_OTHER_USER = "Access denied: cannot filter by other user ID";
    private static final String ACCESS_DENIED_CREATE_FOR_OTHERS = "Access denied: can only create orders for yourself";
    private static final String ACCESS_DENIED_UPDATE_ORDER = "Access denied: cannot update order ";
    private static final String ACCESS_DENIED_CHANGE_OWNER = "Access denied: cannot change order owner";
    private static final String ACCESS_DENIED_ACTIVATE = "Access denied: cannot activate order ";
    private static final String ACCESS_DENIED_DEACTIVATE = "Access denied: cannot deactivate order ";

    private final OrderMapper orderMapper;
    private final OrderWithUserAssembler orderWithUserAssembler;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final UserServiceClient userServiceClient;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public OrderWithUserResponse getOrderById(UUID id) {
        Specification<Order> spec = Specification
            .where(OrderSpecification.hasId(id))
            .and(OrderSpecification.isActive());

        Order order = orderRepository
                .findOne(spec)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + id));

        if (!securityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED_TO_ORDER + id);
        }

        UserInfoResponse userInfo = userServiceClient.fetchUserById(order.getUserId());
        return orderWithUserAssembler.assemble(order, userInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderWithUserResponse getOrderByUserId(Long userId) {
        if (!securityUtils.isOwnerOrAdmin(userId)) {
            throw new AccessDeniedException(ACCESS_DENIED_TO_ORDERS_FOR_USER + userId);
        }

        Specification<Order> spec = Specification
            .where(OrderSpecification.hasUserId(userId))
            .and(OrderSpecification.isActive());

        Order order = orderRepository
                .findOne(spec)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + " with userId: " + userId));

        UserInfoResponse userInfo = userServiceClient.fetchUserById(order.getUserId());
        return orderWithUserAssembler.assemble(order, userInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderWithUserResponse> getAllOrders(Pageable pageable) {
        Specification<Order> spec = Specification.where(OrderSpecification.isActive());


        if (!securityUtils.isCurrentUserAdmin()) {
            Long currentUserId = securityUtils.getCurrentUserId();
            spec = spec.and(OrderSpecification.hasUserId(currentUserId));
        }

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        List<OrderWithUserResponse> content = orders.getContent().stream()
                .map(order -> {
                    UserInfoResponse userInfo = userServiceClient.fetchUserById(order.getUserId());
                    return orderWithUserAssembler.assemble(order, userInfo);
                })
                .toList();

        return new PageImpl<>(content, orders.getPageable(), orders.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderWithUserResponse> getOrdersWithFilter(
            Boolean active,
            String status,
            List<String> statuses,
            Instant createdAtFrom,
            Instant createdAtTo,
            Long userId,
            Pageable pageable) {


        Long effectiveUserId = userId;
        if (!securityUtils.isCurrentUserAdmin()) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (userId != null && !userId.equals(currentUserId)) {
                throw new AccessDeniedException(ACCESS_DENIED_FILTER_BY_OTHER_USER);
            }
            effectiveUserId = currentUserId;
        }

        Specification<Order> spec = (root, query, cb) -> null;

        if (active != null) {
            spec = spec.and(OrderSpecification.hasActive(active));
        }
        if (status != null) {
            spec = spec.and(OrderSpecification.hasStatus(status));
        }
        if (statuses != null && !statuses.isEmpty()) {
            spec = spec.and(OrderSpecification.hasStatuses(statuses));
        }
        if (createdAtFrom != null || createdAtTo != null) {
            spec = spec.and(OrderSpecification.createdAtBetween(createdAtFrom, createdAtTo));
        }
        if (effectiveUserId != null) {
            spec = spec.and(OrderSpecification.hasUserId(effectiveUserId));
        }

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        List<OrderWithUserResponse> content = orders.getContent().stream()
                .map(order -> {
                    UserInfoResponse userInfo = userServiceClient.fetchUserById(order.getUserId());
                    return orderWithUserAssembler.assemble(order, userInfo);
                })
                .toList();

        return new PageImpl<>(content, orders.getPageable(), orders.getTotalElements());
    }

    @Override
    @Transactional
    public OrderWithUserResponse createOrder(CreateOrderRequest createOrderRequest) {
        // Regular users can only create orders for themselves
        if (!securityUtils.isCurrentUserAdmin()) {
            Long currentUserId = securityUtils.getCurrentUserId();
            if (!createOrderRequest.getUserId().equals(currentUserId)) {
                throw new AccessDeniedException(ACCESS_DENIED_CREATE_FOR_OTHERS);
            }
        }

        Order order = orderMapper.toOrder(createOrderRequest);
        if (order.getOrderItems() != null && createOrderRequest.getOrderItems() != null) {
            List<OrderItem> orderItems = order.getOrderItems();
            List<CreateOrderItemRequest> itemRequests = createOrderRequest.getOrderItems();
            for (int i = 0; i < orderItems.size(); i++) {
                OrderItem orderItem = orderItems.get(i);
                orderItem.setOrder(order);
                orderItem.setItem(itemRepository.getReferenceById(itemRequests.get(i).getItemId()));
            }
        }
        Order createdOrder = orderRepository.save(order);
        UserInfoResponse userInfo = userServiceClient.fetchUserById(createdOrder.getUserId());
        return orderWithUserAssembler.assemble(createdOrder, userInfo);
    }

    @Override
    @Transactional
    public OrderWithUserResponse updateOrder(UpdateOrderRequest updateOrderRequest) {
        Order order = orderRepository
            .findById(updateOrderRequest.getId())
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + updateOrderRequest.getId()));

        if (!securityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED_UPDATE_ORDER + updateOrderRequest.getId());
        }

        // Regular users cannot change the order's userId
        if (!securityUtils.isCurrentUserAdmin() && updateOrderRequest.getUserId() != null) {
                throw new AccessDeniedException(ACCESS_DENIED_CHANGE_OWNER);
        }

        orderMapper.updateOrder(updateOrderRequest, order);

        Order updatedOrder = orderRepository.save(order);
        UserInfoResponse userInfo = userServiceClient.fetchUserById(updatedOrder.getUserId());
        return orderWithUserAssembler.assemble(updatedOrder, userInfo);
    }

    @Override
    @Transactional
    public void activateOrder(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + id));

        if (!securityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED_ACTIVATE + id);
        }

        orderRepository.activateOrder(id);
    }

    @Override
    @Transactional
    public void deactivateOrder(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + id));

        if (!securityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED_DEACTIVATE + id);
        }

        orderRepository.deactivateOrder(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderWithUserResponse getOrderWithUserById(UUID orderId, String userEmail) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + orderId));

        if (!securityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED_TO_ORDER + orderId);
        }

        UserInfoResponse userInfo = userServiceClient.fetchUserByEmail(userEmail);
        return orderWithUserAssembler.assemble(order, userInfo);
    }

}
