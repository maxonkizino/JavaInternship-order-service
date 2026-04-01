package com.javainternshiporderservice.service.impl;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import com.javainternshiporderservice.exception.OrderItemNotFoundException;
import com.javainternshiporderservice.mapper.OrderItemMapper;
import com.javainternshiporderservice.model.OrderItem;
import com.javainternshiporderservice.model.specification.OrderItemSpecification;
import com.javainternshiporderservice.repository.OrderItemRepository;
import com.javainternshiporderservice.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private static final String ORDER_ITEM_NOT_FOUND_MESSAGE = "OrderItem not found";

    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderItemResponse getOrderItemById(UUID id) {
        Specification<OrderItem> spec = Specification
            .where(OrderItemSpecification.hasId(id))
            .and(OrderItemSpecification.isActive());

        OrderItem orderItem = orderItemRepository
                .findOne(spec)
                .orElseThrow(() -> new OrderItemNotFoundException(ORDER_ITEM_NOT_FOUND_MESSAGE + " with id: " + id));

        return orderItemMapper.toOrderItemResponse(orderItem);
    }

    @Override
    public OrderItemResponse getOrderItemByOrderId(UUID orderId) {
        Specification<OrderItem> spec = Specification
            .where(OrderItemSpecification.hasOrderId(orderId))
            .and(OrderItemSpecification.isActive());

        OrderItem orderItem = orderItemRepository
                .findOne(spec)
                .stream()
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(ORDER_ITEM_NOT_FOUND_MESSAGE + " with orderId: " + orderId));

        return orderItemMapper.toOrderItemResponse(orderItem);
    }

    @Override
    public OrderItemResponse getOrderItemByItemId(UUID itemId) {

        Specification<OrderItem> spec = Specification
            .where(OrderItemSpecification.hasItemId(itemId))
            .and(OrderItemSpecification.isActive());

        OrderItem orderItem = orderItemRepository
                .findOne(spec)
                .stream()
                .findFirst()
                .orElseThrow(() -> new OrderItemNotFoundException(ORDER_ITEM_NOT_FOUND_MESSAGE + " with itemId: " + itemId));

        return orderItemMapper.toOrderItemResponse(orderItem);
    }

    @Override
    public Page<OrderItemResponse> getAllOrderItems(Pageable pageable) {
        Specification<OrderItem> spec = Specification
            .where(OrderItemSpecification.isActive());

        Page<OrderItem> orderItems = orderItemRepository.findAll(spec, pageable);

        return orderItemMapper.toOrderItemResponsesPage(orderItems);
    }

    @Override
    public OrderItemResponse createOrderItem(CreateOrderItemRequest createOrderItemRequest) {
        OrderItem orderItem = orderItemMapper.toOrderItem(createOrderItemRequest);
        OrderItem createdOrderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toOrderItemResponse(createdOrderItem);
    }

    @Override
    public OrderItemResponse updateOrderItem(UpdateOrderItemRequest updateOrderItemRequest) {
        OrderItem orderItem = orderItemRepository
            .findById(updateOrderItemRequest.getId())
            .orElseThrow(() -> new OrderItemNotFoundException(ORDER_ITEM_NOT_FOUND_MESSAGE + " with id: " + updateOrderItemRequest.getId()));

        orderItemMapper.updateOrderItem(updateOrderItemRequest, orderItem);

        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toOrderItemResponse(updatedOrderItem);
    }

    @Override
    public void activateOrderItem(UUID id) {
        orderItemRepository.activateOrderItem(id);
    }

    @Override
    public void deactivateOrderItem(UUID id) {
        orderItemRepository.deactivateOrderItem(id);
    }

}
