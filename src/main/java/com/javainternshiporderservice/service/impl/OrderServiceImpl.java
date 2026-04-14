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
import com.javainternshiporderservice.repository.OrderItemRepository;
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
    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final UserServiceClient userServiceClient;
    private final SecurityUtils securityUtils;

    private UserInfoResponse loadUserInfoForOrder(Order order) {
        Long ownerId = order.getUserId();
        String jwtEmail = securityUtils.getCurrentUserEmail();
        Long currentUserId = securityUtils.getCurrentUserId();
        boolean sameUser = currentUserId != null && ownerId.equals(currentUserId);
        if (sameUser && jwtEmail != null && !jwtEmail.isBlank()) {
            UserInfoResponse userInfo = userServiceClient.fetchUserByEmail(jwtEmail);
            Long fetchedUserId = userInfo.getId();
            if (fetchedUserId != null && !fetchedUserId.equals(ownerId)) {
                throw new AccessDeniedException("Token email does not match order owner");
            }
            return userInfo;
        }
        return userServiceClient.fetchUserById(ownerId);
    }

    private OrderWithUserResponse assembleWithUser(Order order) {
        return orderWithUserAssembler.assemble(order, loadUserInfoForOrder(order));
    }

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

        return assembleWithUser(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderWithUserResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        if (!securityUtils.isOwnerOrAdmin(userId)) {
            throw new AccessDeniedException(ACCESS_DENIED_TO_ORDERS_FOR_USER + userId);
        }

        Specification<Order> spec = Specification
                .where(OrderSpecification.hasUserId(userId))
                .and(OrderSpecification.isActive());

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        List<OrderWithUserResponse> content = orders.getContent().stream()
                .map(this::assembleWithUser)
                .toList();

        return new PageImpl<>(content, orders.getPageable(), orders.getTotalElements());
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
                .map(this::assembleWithUser)
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

        Specification<Order> spec = (root, query, cb) -> cb.conjunction();

        if (active != null) {
            spec = spec.and(OrderSpecification.hasActive(active));
        } else {
            spec = spec.and(OrderSpecification.isActive());
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
                .map(this::assembleWithUser)
                .toList();

        return new PageImpl<>(content, orders.getPageable(), orders.getTotalElements());
    }

    @Override
    @Transactional
    public OrderWithUserResponse createOrder(CreateOrderRequest createOrderRequest) {
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
        return assembleWithUser(createdOrder);
    }

    @Override
    @Transactional
    public OrderWithUserResponse updateOrder(UUID id, UpdateOrderRequest updateOrderRequest) {
        if (updateOrderRequest.getId() != null && !updateOrderRequest.getId().equals(id)) {
            throw new IllegalArgumentException("Order id in request body must match id in path");
        }
        updateOrderRequest.setId(id);

        Specification<Order> loadSpec = Specification
                .where(OrderSpecification.hasId(id))
                .and(OrderSpecification.isActive());
        Order order = orderRepository
            .findOne(loadSpec)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + id));

        if (!securityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED_UPDATE_ORDER + id);
        }

        if (!securityUtils.isCurrentUserAdmin() && updateOrderRequest.getUserId() != null) {
                throw new AccessDeniedException(ACCESS_DENIED_CHANGE_OWNER);
        }

        orderMapper.updateOrder(updateOrderRequest, order);

        Order updatedOrder = orderRepository.save(order);
        return assembleWithUser(updatedOrder);
    }

    @Override
    @Transactional
    public OrderWithUserResponse activateOrder(UUID id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + id));

        if (!securityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED_ACTIVATE + id);
        }

        orderRepository.activateOrder(id);
        orderItemRepository.activateByOrderId(id);

        Order reactivated = orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + id));
        return assembleWithUser(reactivated);
    }

    @Override
    @Transactional
    public void deactivateOrder(UUID id) {
        Specification<Order> loadSpec = Specification
                .where(OrderSpecification.hasId(id))
                .and(OrderSpecification.isActive());
        Order order = orderRepository.findOne(loadSpec)
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + WITH_ID + id));

        if (!securityUtils.isOwnerOrAdmin(order.getUserId())) {
            throw new AccessDeniedException(ACCESS_DENIED_DEACTIVATE + id);
        }

        orderRepository.deactivateOrder(id);
        orderItemRepository.deactivateByOrderId(id);
    }

}
