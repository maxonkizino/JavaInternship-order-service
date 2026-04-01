package com.javainternshiporderservice.model.specification;

import com.javainternshiporderservice.model.OrderItem;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Specification class for building dynamic queries for OrderItem entities.
 * Provides static factory methods for creating JPA Specifications.
 */
public class OrderItemSpecification {

    private OrderItemSpecification() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a specification to filter order items by ID.
     *
     * @param id the UUID to filter by, null returns null (no filter)
     * @return Specification matching order items with the given ID
     */
    public static Specification<OrderItem> hasId(UUID id) {
        return (root, query, cb) ->
                id == null ? null :
                        cb.equal(root.get("id"), id);
    }

    /**
     * Creates a specification to filter order items by order ID.
     * Navigates through the order relationship.
     *
     * @param orderId the order UUID to filter by, null returns null (no filter)
     * @return Specification matching order items belonging to the order
     */
    public static Specification<OrderItem> hasOrderId(UUID orderId) {
        return (root, query, cb) ->
                orderId == null ? null :
                        cb.equal(root.get("order").get("id"), orderId);
    }

    /**
     * Creates a specification to filter order items by item ID.
     * Navigates through the item relationship.
     *
     * @param itemId the item UUID to filter by, null returns null (no filter)
     * @return Specification matching order items containing the item
     */
    public static Specification<OrderItem> hasItemId(UUID itemId) {
        return (root, query, cb) ->
                itemId == null ? null :
                        cb.equal(root.get("item").get("id"), itemId);
    }

    /**
     * Creates a specification to filter only active order items.
     *
     * @return Specification matching order items where active = true
     */
    public static Specification<OrderItem> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

}
