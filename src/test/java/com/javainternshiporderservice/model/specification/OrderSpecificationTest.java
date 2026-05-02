package com.javainternshiporderservice.model.specification;

import com.javainternshiporderservice.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderSpecificationTest {

    @Test
    void hasId_shouldReturnSpecification_whenIdProvided() {
        UUID id = UUID.randomUUID();
        Specification<Order> spec = OrderSpecification.hasId(id);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasId_shouldReturnSpecification_whenIdIsNull() {
        Specification<Order> spec = OrderSpecification.hasId(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasUserId_shouldReturnSpecification_whenUserIdProvided() {
        Long userId = 1L;
        Specification<Order> spec = OrderSpecification.hasUserId(userId);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasUserId_shouldReturnSpecification_whenUserIdIsNull() {
        Specification<Order> spec = OrderSpecification.hasUserId(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasStatus_shouldReturnSpecification_whenStatusProvided() {
        Specification<Order> spec = OrderSpecification.hasStatus("PENDING");
        assertThat(spec).isNotNull();
    }

    @Test
    void hasStatus_shouldReturnSpecification_whenStatusIsNull() {
        Specification<Order> spec = OrderSpecification.hasStatus(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasStatuses_shouldReturnSpecification_whenStatusesProvided() {
        List<String> statuses = Arrays.asList("PENDING", "CONFIRMED");
        Specification<Order> spec = OrderSpecification.hasStatuses(statuses);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasStatuses_shouldReturnSpecification_whenStatusesIsNull() {
        Specification<Order> spec = OrderSpecification.hasStatuses(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasStatuses_shouldReturnSpecification_whenStatusesIsEmpty() {
        Specification<Order> spec = OrderSpecification.hasStatuses(Collections.emptyList());
        assertThat(spec).isNotNull();
    }

    @Test
    void createdAtBetween_shouldReturnSpecification_whenBothDatesProvided() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        Specification<Order> spec = OrderSpecification.createdAtBetween(from, to);
        assertThat(spec).isNotNull();
    }

    @Test
    void createdAtBetween_shouldReturnSpecification_whenOnlyFromProvided() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Specification<Order> spec = OrderSpecification.createdAtBetween(from, null);
        assertThat(spec).isNotNull();
    }

    @Test
    void createdAtBetween_shouldReturnSpecification_whenOnlyToProvided() {
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        Specification<Order> spec = OrderSpecification.createdAtBetween(null, to);
        assertThat(spec).isNotNull();
    }

    @Test
    void createdAtBetween_shouldReturnSpecification_whenBothDatesAreNull() {
        Specification<Order> spec = OrderSpecification.createdAtBetween(null, null);
        assertThat(spec).isNotNull();
    }

    @Test
    void isActive_shouldReturnNonNullSpecification() {
        Specification<Order> spec = OrderSpecification.isActive();
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldReturnSpecification_whenActiveIsTrue() {
        Specification<Order> spec = OrderSpecification.hasActive(true);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldReturnSpecification_whenActiveIsFalse() {
        Specification<Order> spec = OrderSpecification.hasActive(false);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldReturnSpecification_whenActiveIsNull() {
        Specification<Order> spec = OrderSpecification.hasActive(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void specificationsCanBeChained() {
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        Specification<Order> spec = Specification.where(OrderSpecification.hasId(id))
                .and(OrderSpecification.hasUserId(userId))
                .and(OrderSpecification.hasStatus("PENDING"))
                .and(OrderSpecification.isActive());
        assertThat(spec).isNotNull();
    }

    @Test
    void complexSpecificationChainWithAllFilters() {
        UUID id = UUID.randomUUID();
        Long userId = 1L;
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-12-31T23:59:59Z");
        List<String> statuses = Arrays.asList("PENDING", "CONFIRMED");
        
        Specification<Order> spec = Specification.where(OrderSpecification.hasId(id))
                .and(OrderSpecification.hasUserId(userId))
                .and(OrderSpecification.hasStatus("PENDING"))
                .and(OrderSpecification.hasStatuses(statuses))
                .and(OrderSpecification.createdAtBetween(from, to))
                .and(OrderSpecification.hasActive(true))
                .and(OrderSpecification.isActive());
        assertThat(spec).isNotNull();
    }

    @Test
    void specificationChainWithAllNulls() {
        Specification<Order> spec = Specification.where(OrderSpecification.hasId(null))
                .and(OrderSpecification.hasUserId(null))
                .and(OrderSpecification.hasStatus(null))
                .and(OrderSpecification.hasStatuses(null))
                .and(OrderSpecification.createdAtBetween(null, null))
                .and(OrderSpecification.hasActive(null));
        assertThat(spec).isNotNull();
    }
}
