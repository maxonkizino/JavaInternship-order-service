package com.javainternshiporderservice.repository;

import com.javainternshiporderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends
                                JpaRepository<Order, UUID>,
                                JpaSpecificationExecutor<Order> {

    @EntityGraph(attributePaths = {"orderItems"})
    @Override
    Optional<Order> findById(UUID id);

    @EntityGraph(attributePaths = {"orderItems"})
    List<Order> findByUserId(UUID userId);

    @EntityGraph(attributePaths = {"orderItems"})
    @Override
    Page<Order> findAll(Specification<Order> specification, Pageable pageable);

    @Transactional
    void activateOrder(UUID id);
                                        
    @Transactional
    void deactivateOrder(UUID id);

}
