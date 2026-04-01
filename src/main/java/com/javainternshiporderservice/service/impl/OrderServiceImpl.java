package com.javainternshiporderservice.service.impl;

import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderResponse;
import com.javainternshiporderservice.exception.OrderNotFoundException;
import com.javainternshiporderservice.mapper.OrderMapper;
import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.model.specification.OrderSpecification;
import com.javainternshiporderservice.repository.OrderRepository;
import com.javainternshiporderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found";

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;

    @Override
    public OrderResponse getOrderById(UUID id) {
        Specification<Order> spec = Specification
            .where(OrderSpecification.hasId(id))
            .and(OrderSpecification.isActive());

        Order order = orderRepository
                .findOne(spec)
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + " with id: " + id));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderByUserId(UUID userId) {
        Specification<Order> spec = Specification
            .where(OrderSpecification.hasUserId(userId))
            .and(OrderSpecification.isActive());

        Order order = orderRepository
                .findOne(spec)
                .stream()
                .findFirst()
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + " with userId: " + userId));

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Specification<Order> spec = Specification
            .where(OrderSpecification.isActive());

        Page<Order> orders = orderRepository.findAll(spec, pageable);

        return orderMapper.toOrderResponsesPage(orders);
    }

    @Override
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        Order order = orderMapper.toOrder(createOrderRequest);
        Order createdOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(createdOrder);
    }

    @Override
    public OrderResponse updateOrder(UpdateOrderRequest updateOrderRequest) {
        Order order = orderRepository
            .findById(updateOrderRequest.getId())
            .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + " with id: " + updateOrderRequest.getId()));

        orderMapper.updateOrder(updateOrderRequest, order);

        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    public void activateOrder(UUID id) {
        orderRepository.activateOrder(id);
    }

    @Override
    public void deactivateOrder(UUID id) {
        orderRepository.deactivateOrder(id);
    }

}
