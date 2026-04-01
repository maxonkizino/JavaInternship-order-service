package com.javainternshiporderservice.model.specification;

import com.javainternshiporderservice.model.Order;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Specification class for building dynamic queries for Order entities.
 * Provides static factory methods for creating JPA Specifications.
 */
public class OrderSpecification {

    private OrderSpecification() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a specification to filter orders by ID.
     *
     * @param id the UUID to filter by, null returns null (no filter)
     * @return Specification matching orders with the given ID
     */
    public static Specification<Order> hasId(UUID id) {
        return (root, query, cb) ->
                id == null ? null :
                        cb.equal(root.get("id"), id);
    }

    /**
     * Creates a specification to filter orders by user ID.
     *
     * @param userId the user UUID to filter by, null returns null (no filter)
     * @return Specification matching orders belonging to the user
     */
    public static Specification<Order> hasUserId(UUID userId) {
        return (root, query, cb) ->
                userId == null ? null :
                        cb.equal(root.get("userId"), userId);
    }

    /**
     * Creates a specification to filter orders by status.
     *
     * @param status the status string to filter by, null returns null (no filter)
     * @return Specification matching orders with the given status
     */
    public static Specification<Order> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("status"), status);
    }

    /**
     * Creates a specification to filter orders by multiple statuses (IN clause).
     *
     * @param statuses the list of status strings to filter by,
     *                 null or empty returns null (no filter)
     * @return Specification matching orders with any of the given statuses
     */
    public static Specification<Order> hasStatuses(List<String> statuses) {
        return (root, query, cb) ->
                statuses == null || statuses.isEmpty() ? null :
                        root.get("status").in(statuses);
    }

    /**
     * Creates a specification to filter orders by creation date range.
     * Supports partial ranges (only from or only to).
     *
     * @param from the start date (inclusive), null means no lower bound
     * @param to   the end date (inclusive), null means no upper bound
     * @return Specification matching orders within the date range
     */
    public static Specification<Order> createdAtBetween(Instant from, Instant to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return null;
            }
            if (from == null) {
                return cb.lessThanOrEqualTo(root.get("createdAt"), to);
            }
            if (to == null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), from);
            }
            return cb.between(root.get("createdAt"), from, to);
        };
    }

    /**
     * Creates a specification to filter only active orders.
     *
     * @return Specification matching orders where active = true
     */
    public static Specification<Order> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

}
