package com.javainternshiporderservice.service.impl;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import com.javainternshiporderservice.exception.ItemNotFoundException;
import com.javainternshiporderservice.exception.OrderItemNotFoundException;
import com.javainternshiporderservice.exception.OrderNotFoundException;
import com.javainternshiporderservice.mapper.OrderItemMapper;
import com.javainternshiporderservice.model.Item;
import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.model.OrderItem;
import com.javainternshiporderservice.model.specification.OrderItemSpecification;
import com.javainternshiporderservice.repository.ItemRepository;
import com.javainternshiporderservice.repository.OrderItemRepository;
import com.javainternshiporderservice.repository.OrderRepository;
import com.javainternshiporderservice.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private static final String ORDER_ITEM_NOT_FOUND_MESSAGE = "OrderItem not found";
    private static final String ORDER_NOT_FOUND_MESSAGE = "Order not found with id: ";
    private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found with id: ";

    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Page<OrderItemResponse> getAllOrderItems(Pageable pageable) {
        Specification<OrderItem> spec = Specification
            .where(OrderItemSpecification.isActive());

        Page<OrderItem> orderItems = orderItemRepository.findAll(spec, pageable);

        return orderItemMapper.toOrderItemResponsesPage(orderItems);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderItemResponse> getOrderItemsWithFilter(
            Boolean active,
            UUID orderId,
            UUID itemId,
            Pageable pageable) {

        Specification<OrderItem> spec = (root, query, cb) -> null;

        if (active != null) {
            spec = spec.and(OrderItemSpecification.hasActive(active));
        }
        if (orderId != null) {
            spec = spec.and(OrderItemSpecification.hasOrderId(orderId));
        }
        if (itemId != null) {
            spec = spec.and(OrderItemSpecification.hasItemId(itemId));
        }

        Page<OrderItem> orderItems = orderItemRepository.findAll(spec, pageable);
        return orderItemMapper.toOrderItemResponsesPage(orderItems);
    }

    @Override
    @Transactional
    public OrderItemResponse createOrderItem(CreateOrderItemRequest createOrderItemRequest) {
        Order order = orderRepository.findById(createOrderItemRequest.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(ORDER_NOT_FOUND_MESSAGE + createOrderItemRequest.getOrderId()));
        Item item = itemRepository.findById(createOrderItemRequest.getItemId())
                .orElseThrow(() -> new ItemNotFoundException(ITEM_NOT_FOUND_MESSAGE + createOrderItemRequest.getItemId()));

        OrderItem orderItem = orderItemMapper.toOrderItem(createOrderItemRequest);
        orderItem.setOrder(order);
        orderItem.setItem(item);

        OrderItem createdOrderItem = orderItemRepository.saveAndFlush(orderItem);
        BigDecimal currentTotal = order.getTotalPrice() != null ? order.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(createdOrderItem.getQuantity()));
        order.setTotalPrice(currentTotal.add(lineTotal));
        orderRepository.saveAndFlush(order);

        return orderItemMapper.toOrderItemResponse(createdOrderItem);
    }

    @Override
    @Transactional
    public OrderItemResponse updateOrderItem(UpdateOrderItemRequest updateOrderItemRequest) {
        OrderItem orderItem = orderItemRepository
            .findById(updateOrderItemRequest.getId())
            .orElseThrow(() -> new OrderItemNotFoundException(ORDER_ITEM_NOT_FOUND_MESSAGE + " with id: " + updateOrderItemRequest.getId()));

        orderItemMapper.updateOrderItem(updateOrderItemRequest, orderItem);

        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
        return orderItemMapper.toOrderItemResponse(updatedOrderItem);
    }

    @Override
    @Transactional
    public void activateOrderItem(UUID id) {
        orderItemRepository.activateOrderItem(id);
    }

    @Override
    @Transactional
    public void deactivateOrderItem(UUID id) {
        orderItemRepository.deactivateOrderItem(id);
    }

}
