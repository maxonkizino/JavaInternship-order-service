package com.javainternshiporderservice.repository;

import com.javainternshiporderservice.model.OrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends
                                JpaRepository<OrderItem, UUID>,
                                JpaSpecificationExecutor<OrderItem> {

    @EntityGraph(attributePaths = {"order", "item"})
    @Override
    Optional<OrderItem> findById(UUID id);

    @EntityGraph(attributePaths = {"order", "item"})
    List<OrderItem> findByOrderId(UUID orderId);

    @EntityGraph(attributePaths = {"order", "item"})
    List<OrderItem> findByItemId(UUID itemId);

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.active = true WHERE oi.id = :id")
    void activateOrderItem(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.active = false WHERE oi.id = :id")
    void deactivateOrderItem(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.active = false WHERE oi.order.id = :orderId")
    void deactivateByOrderId(@Param("orderId") UUID orderId);

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.active = true WHERE oi.order.id = :orderId")
    void activateByOrderId(@Param("orderId") UUID orderId);

}
