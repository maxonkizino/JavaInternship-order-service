package com.javainternshiporderservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.javainternshiporderservice.model.OrderItem;
import java.util.UUID;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Optional;

public interface OrderItemRepository extends
                                JpaRepository<OrderItem, UUID>,
                                JpaSpecificationExecutor<OrderItem> {

@EntityGraph(attributePaths = {"order", "item"})
Optional<OrderItem> findById(UUID id);

@EntityGraph(attributePaths = {"order", "item"})
List<OrderItem> findByOrderId(UUID orderId);

@EntityGraph(attributePaths = {"order", "item"})
List<OrderItem> findByItemId(UUID itemId);

Page<OrderItem> findAll(Specification<OrderItem> specification, Pageable pageable);

@Transactional
void createOrderItem(OrderItem orderItem);

@Transactional
void updateOrderItem(OrderItem orderItem);

@Transactional
void activateOrderItem(UUID id);

@Transactional
void deactivateOrderItem(UUID id);

}
