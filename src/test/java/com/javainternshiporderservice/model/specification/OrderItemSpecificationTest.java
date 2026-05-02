package com.javainternshiporderservice.model.specification;

import com.javainternshiporderservice.model.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemSpecificationTest {

    @Test
    void hasId_shouldReturnSpecification_whenIdProvided() {
        UUID id = UUID.randomUUID();
        Specification<OrderItem> spec = OrderItemSpecification.hasId(id);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasId_shouldReturnSpecification_whenIdIsNull() {
        Specification<OrderItem> spec = OrderItemSpecification.hasId(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasOrderId_shouldReturnSpecification_whenOrderIdProvided() {
        UUID orderId = UUID.randomUUID();
        Specification<OrderItem> spec = OrderItemSpecification.hasOrderId(orderId);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasOrderId_shouldReturnSpecification_whenOrderIdIsNull() {
        Specification<OrderItem> spec = OrderItemSpecification.hasOrderId(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasItemId_shouldReturnSpecification_whenItemIdProvided() {
        UUID itemId = UUID.randomUUID();
        Specification<OrderItem> spec = OrderItemSpecification.hasItemId(itemId);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasItemId_shouldReturnSpecification_whenItemIdIsNull() {
        Specification<OrderItem> spec = OrderItemSpecification.hasItemId(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void isActive_shouldReturnNonNullSpecification() {
        Specification<OrderItem> spec = OrderItemSpecification.isActive();
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldReturnSpecification_whenActiveIsTrue() {
        Specification<OrderItem> spec = OrderItemSpecification.hasActive(true);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldReturnSpecification_whenActiveIsFalse() {
        Specification<OrderItem> spec = OrderItemSpecification.hasActive(false);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldReturnSpecification_whenActiveIsNull() {
        Specification<OrderItem> spec = OrderItemSpecification.hasActive(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void specificationsCanBeChained() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        Specification<OrderItem> spec = Specification.where(OrderItemSpecification.hasOrderId(orderId))
                .and(OrderItemSpecification.hasItemId(itemId))
                .and(OrderItemSpecification.isActive());
        assertThat(spec).isNotNull();
    }

    @Test
    void complexSpecificationChainWithMultipleFilters() {
        UUID id = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        
        Specification<OrderItem> spec = Specification.where(OrderItemSpecification.hasId(id))
                .and(OrderItemSpecification.hasOrderId(orderId))
                .and(OrderItemSpecification.hasItemId(itemId))
                .and(OrderItemSpecification.hasActive(true));
        assertThat(spec).isNotNull();
    }

    @Test
    void specificationChainWithAllNulls() {
        Specification<OrderItem> spec = Specification.where(OrderItemSpecification.hasId(null))
                .and(OrderItemSpecification.hasOrderId(null))
                .and(OrderItemSpecification.hasItemId(null))
                .and(OrderItemSpecification.hasActive(null));
        assertThat(spec).isNotNull();
    }
}
