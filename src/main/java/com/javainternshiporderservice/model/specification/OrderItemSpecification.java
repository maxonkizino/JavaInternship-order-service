package com.javainternshiporderservice.model.specification;

import org.springframework.data.jpa.domain.Specification;
import com.javainternshiporderservice.model.OrderItem;
import java.util.UUID;

public class OrderItemSpecification {

    private OrderItemSpecification() {
    }

    public static Specification<OrderItem> hasOrderId(UUID orderId) {
        return (root, query, cb) ->
                orderId == null ? null :
                        cb.equal(root.get("orderId"), orderId);
    }

    public static Specification<OrderItem> hasItemId(UUID itemId) {
        return (root, query, cb) ->
                itemId == null ? null :
                        cb.equal(root.get("itemId"), itemId);
    }
    public static Specification<OrderItem> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
   
}
