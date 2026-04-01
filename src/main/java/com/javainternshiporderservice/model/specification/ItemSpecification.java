package com.javainternshiporderservice.model.specification;


import org.springframework.data.jpa.domain.Specification;
import com.javainternshiporderservice.model.Item;
import java.math.BigDecimal;


public class ItemSpecification {
    private ItemSpecification() {

    }

    public static Specification<Item> hasName(String name) {
        return (root, query, cb) ->
                name == null ? null :
                        cb.like(cb.lower(root.get("name")),
                                "%" + name.toLowerCase() + "%");
    }

    public static Specification<Item> hasPrice(BigDecimal price) {
        return (root, query, cb) ->
                price == null ? null :
                        cb.equal(root.get("price"), price);
    }

    public static Specification<Item> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }
}
