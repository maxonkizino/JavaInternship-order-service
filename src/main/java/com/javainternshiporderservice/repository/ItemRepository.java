package com.javainternshiporderservice.repository;

import com.javainternshiporderservice.model.Item;
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
public interface ItemRepository extends
                                JpaRepository<Item, UUID>,
                                JpaSpecificationExecutor<Item> {

    @EntityGraph(attributePaths = {"orderItems"})
    @Override
    Optional<Item> findById(UUID id);

    @EntityGraph(attributePaths = {"orderItems"})
    Optional<Item> findByName(String name);

    @Modifying
    @Query("UPDATE Item i SET i.active = true WHERE i.id = :id")
    void activateItem(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Item i SET i.active = false WHERE i.id = :id")
    void deactivateItem(@Param("id") UUID id);

}
