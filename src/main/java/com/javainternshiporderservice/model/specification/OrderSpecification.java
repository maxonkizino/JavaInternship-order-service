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

    private static final String CREATED_AT = "createdAt";

    private OrderSpecification() {}

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
    public static Specification<Order> hasUserId(Long userId) {
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
                return cb.lessThanOrEqualTo(root.get(CREATED_AT), to);
            }
            if (to == null) {
                return cb.greaterThanOrEqualTo(root.get(CREATED_AT), from);
            }
            return cb.between(root.get(CREATED_AT), from, to);
        };
    }

    /**
     * Creates a specification to filter only non-deleted orders.
     *
     * @return Specification matching orders where deleted = false
     */
    public static Specification<Order> isActive() {
        return (root, query, cb) -> cb.isFalse(root.get("deleted"));
    }

    /**
     * Filters by "active" in API terms: {@code true} = not deleted, {@code false} = soft-deleted.
     *
     * @param active null means no filter
     */
    public static Specification<Order> hasActive(Boolean active) {
        return (root, query, cb) ->
                active == null ? null :
                        cb.equal(root.get("deleted"), !active);
    }

}
