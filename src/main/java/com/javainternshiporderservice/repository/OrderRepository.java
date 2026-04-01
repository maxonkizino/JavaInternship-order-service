package com.javainternshiporderservice.repository;

import com.javainternshiporderservice.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends
                                JpaRepository<Order, UUID>,
                                JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"orderItems"})
    @Override
    Optional<Order> findById(UUID id);

    @Modifying
    @Query("UPDATE Order o SET o.active = true WHERE o.id = :id")
    void activateOrder(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Order o SET o.active = false WHERE o.id = :id")
    void deactivateOrder(@Param("id") UUID id);

}
