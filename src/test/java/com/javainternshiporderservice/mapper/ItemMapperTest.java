package com.javainternshiporderservice.mapper;

import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private ItemMapperImpl itemMapper;

    @BeforeEach
    void setUp() {
        itemMapper = new ItemMapperImpl();
    }

    @Test
    void toItemResponse_shouldMapAllFields() {
        UUID id = UUID.randomUUID();
        Item item = new Item();
        item.setId(id);
        item.setName("Test Item");
        item.setPrice(new BigDecimal("99.99"));
        item.setActive(true);

        ItemResponse response = itemMapper.toItemResponse(item);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Test Item");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void toItemResponse_shouldReturnNullForNullItem() {
        ItemResponse response = itemMapper.toItemResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toItem_shouldMapAllFields() {
        CreateItemRequest request = new CreateItemRequest();
        request.setName("New Item");
        request.setPrice(new BigDecimal("49.99"));
        request.setActive(true);

        Item item = itemMapper.toItem(request);

        assertThat(item).isNotNull();
        assertThat(item.getName()).isEqualTo("New Item");
        assertThat(item.getPrice()).isEqualTo(new BigDecimal("49.99"));
        assertThat(item.isActive()).isTrue();
    }

    @Test
    void toItem_shouldReturnNullForNullRequest() {
        Item item = itemMapper.toItem(null);
        assertThat(item).isNull();
    }

    @Test
    void updateItem_shouldUpdateNonNullFields() {
        UUID id = UUID.randomUUID();
        Item item = new Item();
        item.setId(id);
        item.setName("Old Name");
        item.setPrice(new BigDecimal("10.00"));
        item.setActive(false);

        UpdateItemRequest request = new UpdateItemRequest();
        request.setId(id);
        request.setName("Updated Name");
        request.setPrice(new BigDecimal("20.00"));

        itemMapper.updateItem(request, item);

        assertThat(item.getName()).isEqualTo("Updated Name");
        assertThat(item.getPrice()).isEqualTo(new BigDecimal("20.00"));
        assertThat(item.isActive()).isFalse();
    }

    @Test
    void updateItem_shouldIgnoreNullFields() {
        UUID id = UUID.randomUUID();
        Item item = new Item();
        item.setId(id);
        item.setName("Original Name");
        item.setPrice(new BigDecimal("10.00"));

        UpdateItemRequest request = new UpdateItemRequest();
        request.setId(id);

        itemMapper.updateItem(request, item);

        assertThat(item.getName()).isEqualTo("Original Name");
        assertThat(item.getPrice()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void toItemResponses_shouldMapList() {
        Item item1 = new Item();
        item1.setId(UUID.randomUUID());
        item1.setName("Item 1");
        item1.setPrice(new BigDecimal("10.00"));

        Item item2 = new Item();
        item2.setId(UUID.randomUUID());
        item2.setName("Item 2");
        item2.setPrice(new BigDecimal("20.00"));

        List<Item> items = Arrays.asList(item1, item2);
        List<ItemResponse> responses = itemMapper.toItemResponses(items);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("Item 1");
        assertThat(responses.get(1).getName()).isEqualTo("Item 2");
    }

    @Test
    void toItemResponses_shouldReturnNullForNullList() {
        List<ItemResponse> responses = itemMapper.toItemResponses(null);
        assertThat(responses).isNull();
    }

    @Test
    void toItemResponses_shouldReturnEmptyListForEmptyInput() {
        List<ItemResponse> responses = itemMapper.toItemResponses(Collections.emptyList());
        assertThat(responses).isEmpty();
    }

    @Test
    void toItemResponsesPage_shouldMapPage() {
        Item item = new Item();
        item.setId(UUID.randomUUID());
        item.setName("Paged Item");
        item.setPrice(new BigDecimal("30.00"));

        Page<Item> itemPage = new PageImpl<>(
            Collections.singletonList(item),
            PageRequest.of(0, 10),
            1
        );

        Page<ItemResponse> responsePage = itemMapper.toItemResponsesPage(itemPage);

        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getContent()).hasSize(1);
        assertThat(responsePage.getContent().get(0).getName()).isEqualTo("Paged Item");
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        assertThat(responsePage.getNumber()).isEqualTo(0);
    }

    @Test
    void toItemResponsesPage_shouldReturnNullForNullPage() {
        Page<ItemResponse> responsePage = itemMapper.toItemResponsesPage(null);
        assertThat(responsePage).isNull();
    }

    @Test
    void toItemResponsesPage_shouldHandleMultipleItems() {
        Item item1 = new Item();
        item1.setId(UUID.randomUUID());
        item1.setName("Item 1");

        Item item2 = new Item();
        item2.setId(UUID.randomUUID());
        item2.setName("Item 2");

        Item item3 = new Item();
        item3.setId(UUID.randomUUID());
        item3.setName("Item 3");

        Page<Item> itemPage = new PageImpl<>(
            Arrays.asList(item1, item2, item3),
            PageRequest.of(1, 10),
            3
        );

        Page<ItemResponse> responsePage = itemMapper.toItemResponsesPage(itemPage);

        assertThat(responsePage.getContent()).hasSize(3);
        assertThat(responsePage.getNumber()).isEqualTo(1);
    }
}
