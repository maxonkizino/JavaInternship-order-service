package com.javainternshiporderservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.javainternshiporderservice.model.Item;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.Optional;

public interface ItemRepository extends
                                JpaRepository<Item, UUID>,
                                JpaSpecificationExecutor<Item> {

@EntityGraph(attributePaths = {"orderItems"})
Optional<Item> findById(UUID id);

Page<Item> findAll(Specification<Item> specification, Pageable pageable);

@Transactional
void createItem(Item item);

@Transactional
void updateItem(Item item);

@Transactional
void activateItem(UUID id);

@Transactional
void deactivateItem(UUID id);

}
