package com.javainternshiporderservice.service.impl;

import com.javainternshiporderservice.client.UserServiceClient;
import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderWithUserResponse;
import com.javainternshiporderservice.dto.response.UserInfoResponse;
import com.javainternshiporderservice.exception.OrderNotFoundException;
import com.javainternshiporderservice.mapper.OrderMapper;
import com.javainternshiporderservice.mapper.OrderWithUserAssembler;
import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.model.specification.OrderSpecification;
import com.javainternshiporderservice.repository.OrderRepository;
import com.javainternshiporderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found";

    private final OrderMapper orderMapper;
    private final OrderWithUserAssembler orderWithUserAssembler;
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public OrderWithUserResponse getOrderById(UUID id) {
        Specification<Order> spec = Specification
            .where(OrderSpecification.hasId(id))
            .and(OrderSpecification.isActive());

        Order order = orderRepository
                .findOne(spec)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + " with id: " + id));

        UserInfoResponse userInfo = userServiceClient.fetchUserById(order.getUserId());
        return orderWithUserAssembler.assemble(order, userInfo);
    }

    @Override
    public OrderWithUserResponse getOrderByUserId(Long userId) {
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
    public Page<OrderWithUserResponse> getAllOrders(Pageable pageable) {
        Specification<Order> spec = Specification
            .where(OrderSpecification.isActive());

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        List<OrderWithUserResponse> content = orders.getContent().stream()
                .map(order -> {
                    UserInfoResponse userInfo = userServiceClient.fetchUserById(order.getUserId());
                    return orderWithUserAssembler.assemble(order, userInfo);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(content, orders.getPageable(), orders.getTotalElements());
    }

    @Override
    @Transactional
    public OrderWithUserResponse createOrder(CreateOrderRequest createOrderRequest) {
        Order order = orderMapper.toOrder(createOrderRequest);
        Order createdOrder = orderRepository.save(order);
        UserInfoResponse userInfo = userServiceClient.fetchUserById(createdOrder.getUserId());
        return orderWithUserAssembler.assemble(createdOrder, userInfo);
    }

    @Override
    @Transactional
    public OrderWithUserResponse updateOrder(UpdateOrderRequest updateOrderRequest) {
        Order order = orderRepository
            .findById(updateOrderRequest.getId())
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + " with id: " + updateOrderRequest.getId()));

        orderMapper.updateOrder(updateOrderRequest, order);

        Order updatedOrder = orderRepository.save(order);
        UserInfoResponse userInfo = userServiceClient.fetchUserById(updatedOrder.getUserId());
        return orderWithUserAssembler.assemble(updatedOrder, userInfo);
    }

    @Override
    @Transactional
    public void activateOrder(UUID id) {
        orderRepository.activateOrder(id);
    }

    @Override
    @Transactional
    public void deactivateOrder(UUID id) {
        orderRepository.deactivateOrder(id);
    }

    @Override
    public OrderWithUserResponse getOrderWithUserById(UUID orderId, String userEmail) {
        Order order = orderRepository
                .findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + " with id: " + orderId));

        UserInfoResponse userInfo = userServiceClient.fetchUserByEmail(userEmail);
        return orderWithUserAssembler.assemble(order, userInfo);
    }

}
