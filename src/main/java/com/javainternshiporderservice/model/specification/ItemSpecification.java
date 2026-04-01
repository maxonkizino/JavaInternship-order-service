package com.javainternshiporderservice.model.specification;

import com.javainternshiporderservice.model.Item;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Specification class for building dynamic queries for Item entities.
 * Provides static factory methods for creating JPA Specifications.
 */
public class ItemSpecification {

    private ItemSpecification() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a specification to filter items by ID.
     *
     * @param id the UUID to filter by, null returns null (no filter)
     * @return Specification matching items with the given ID
     */
    public static Specification<Item> hasId(UUID id) {
        return (root, query, cb) ->
                id == null ? null :
                        cb.equal(root.get("id"), id);
    }

    /**
     * Creates a specification to filter items by name using case-insensitive LIKE.
     * Supports partial matching (e.g., "pho" matches "Phone").
     *
     * @param name the name string to filter by, null returns null (no filter)
     * @return Specification matching items with name containing the given string
     */
    public static Specification<Item> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%");
    }

    /**
     * Creates a specification to filter items by exact price.
     *
     * @param price the price to filter by, null returns null (no filter)
     * @return Specification matching items with the given price
     */
    public static Specification<Item> hasPrice(BigDecimal price) {
        return (root, query, cb) ->
                price == null ? null :
                        cb.equal(root.get("price"), price);
    }

    /**
     * Creates a specification to filter only active items.
     *
     * @return Specification matching items where active = true
     */
    public static Specification<Item> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

}
