package com.javainternshiporderservice.model.specification;

import org.springframework.data.jpa.domain.Specification;
import com.javainternshiporderservice.model.Order;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderSpecification {

    private OrderSpecification() {
    }

    public static Specification<Order> hasUserId(UUID userId) {
        return (root, query, cb) ->
                userId == null ? null :
                        cb.equal(root.get("userId"), userId);
    }

    public static Specification<Order> hasStatus(String status) {
        return (root, query, cb) ->
                status == null ? null :
                        cb.equal(root.get("status"), status);
    }

    public static Specification<Order> hasStatuses(List<String> statuses) {
        return (root, query, cb) ->
                statuses == null || statuses.isEmpty() ? null :
                        root.get("status").in(statuses);
    }

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

    public static Specification<Order> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

}
