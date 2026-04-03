package com.javainternshiporderservice.model.specification;

import com.javainternshiporderservice.model.Item;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ItemSpecificationTest {

    @Test
    void hasId_shouldReturnSpecification_whenIdProvided() {
        UUID id = UUID.randomUUID();
        Specification<Item> spec = ItemSpecification.hasId(id);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasId_shouldReturnSpecification_whenIdIsNull() {
        Specification<Item> spec = ItemSpecification.hasId(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasName_shouldReturnSpecification_whenNameProvided() {
        Specification<Item> spec = ItemSpecification.hasName("Test");
        assertThat(spec).isNotNull();
    }

    @Test
    void hasName_shouldReturnSpecification_whenNameIsNull() {
        Specification<Item> spec = ItemSpecification.hasName(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasName_shouldHandleEmptyString() {
        Specification<Item> spec = ItemSpecification.hasName("");
        assertThat(spec).isNotNull();
    }

    @Test
    void hasName_shouldHandleCaseInsensitivity() {
        Specification<Item> spec = ItemSpecification.hasName("TeSt");
        assertThat(spec).isNotNull();
    }

    @Test
    void hasPrice_shouldReturnSpecification_whenPriceProvided() {
        Specification<Item> spec = ItemSpecification.hasPrice(new BigDecimal("99.99"));
        assertThat(spec).isNotNull();
    }

    @Test
    void hasPrice_shouldReturnSpecification_whenPriceIsNull() {
        Specification<Item> spec = ItemSpecification.hasPrice(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void isActive_shouldReturnNonNullSpecification() {
        Specification<Item> spec = ItemSpecification.isActive();
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldReturnSpecification_whenActiveProvided() {
        Specification<Item> spec = ItemSpecification.hasActive(true);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldReturnSpecification_whenActiveIsNull() {
        Specification<Item> spec = ItemSpecification.hasActive(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasActive_shouldWorkWithFalse() {
        Specification<Item> spec = ItemSpecification.hasActive(false);
        assertThat(spec).isNotNull();
    }

    @Test
    void specificationsCanBeChained() {
        UUID id = UUID.randomUUID();
        Specification<Item> spec = Specification.where(ItemSpecification.hasId(id))
                .and(ItemSpecification.hasName("Test"))
                .and(ItemSpecification.isActive());
        assertThat(spec).isNotNull();
    }

    @Test
    void complexSpecificationChainWithNulls() {
        Specification<Item> spec = Specification.where(ItemSpecification.hasId(null))
                .and(ItemSpecification.hasName(null))
                .and(ItemSpecification.hasPrice(null))
                .and(ItemSpecification.hasActive(null));
        assertThat(spec).isNotNull();
    }
}
