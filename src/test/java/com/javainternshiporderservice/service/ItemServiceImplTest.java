package com.javainternshiporderservice.service;

import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.exception.ItemNotFoundException;
import com.javainternshiporderservice.mapper.ItemMapper;
import com.javainternshiporderservice.model.Item;
import com.javainternshiporderservice.repository.ItemRepository;
import com.javainternshiporderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private UUID itemId;
    private Item item;
    private ItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        item = new Item();
        item.setId(itemId);
        item.setName("Test Item");
        item.setPrice(new BigDecimal("50.00"));
        item.setActive(true);

        itemResponse = new ItemResponse();
        itemResponse.setId(itemId);
        itemResponse.setName("Test Item");
        itemResponse.setPrice(new BigDecimal("50.00"));
    }

    @Test
    void getItemById_shouldReturnItem_whenItemExists() {
        // given
        when(itemRepository.findOne(any(Specification.class))).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        // when
        ItemResponse result = itemService.getItemById(itemId);

        // then
        assertThat(result).isEqualTo(itemResponse);
        verify(itemRepository).findOne(any(Specification.class));
    }

    @Test
    void getItemById_shouldThrowItemNotFoundException_whenItemNotFound() {
        // given
        when(itemRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itemService.getItemById(itemId))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Item not found with id: " + itemId);
    }

    @Test
    void getItemByName_shouldReturnItem_whenItemExists() {
        // given
        String itemName = "Test Item";
        when(itemRepository.findOne(any(Specification.class))).thenReturn(Optional.of(item));
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        // when
        ItemResponse result = itemService.getItemByName(itemName);

        // then
        assertThat(result).isEqualTo(itemResponse);
    }

    @Test
    void getAllItems_shouldReturnPageOfItems() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(Collections.singletonList(item));

        when(itemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(itemPage);
        when(itemMapper.toItemResponsesPage(itemPage)).thenReturn(new PageImpl<>(Collections.singletonList(itemResponse)));

        // when
        Page<ItemResponse> result = itemService.getAllItems(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(itemResponse);
    }

    @Test
    void createItem_shouldCreateAndReturnItem() {
        // given
        CreateItemRequest request = new CreateItemRequest();
        request.setName("New Item");
        request.setPrice(new BigDecimal("75.00"));

        when(itemMapper.toItem(request)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        // when
        ItemResponse result = itemService.createItem(request);

        // then
        assertThat(result).isEqualTo(itemResponse);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_shouldUpdateAndReturnItem() {
        // given
        UpdateItemRequest request = new UpdateItemRequest();
        request.setId(itemId);
        request.setName("Updated Item");
        request.setPrice(new BigDecimal("100.00"));

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toItemResponse(item)).thenReturn(itemResponse);

        // when
        ItemResponse result = itemService.updateItem(request);

        // then
        assertThat(result).isEqualTo(itemResponse);
        verify(itemMapper).updateItem(request, item);
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_shouldThrowItemNotFoundException_whenItemNotFound() {
        // given
        UpdateItemRequest request = new UpdateItemRequest();
        request.setId(itemId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> itemService.updateItem(request))
                .isInstanceOf(ItemNotFoundException.class)
                .hasMessageContaining("Item not found with id: " + itemId);
    }

    @Test
    void activateItem_shouldCallRepositoryActivate() {
        // given
        doNothing().when(itemRepository).activateItem(itemId);

        // when
        itemService.activateItem(itemId);

        // then
        verify(itemRepository).activateItem(itemId);
    }

    @Test
    void deactivateItem_shouldCallRepositoryDeactivate() {
        // given
        doNothing().when(itemRepository).deactivateItem(itemId);

        // when
        itemService.deactivateItem(itemId);

        // then
        verify(itemRepository).deactivateItem(itemId);
    }

    @Test
    void getItemsWithFilter_shouldApplyActiveFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(Collections.singletonList(item));

        when(itemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(itemPage);
        when(itemMapper.toItemResponsesPage(itemPage)).thenReturn(new PageImpl<>(Collections.singletonList(itemResponse)));

        // when
        Page<ItemResponse> result = itemService.getItemsWithFilter(false, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getItemsWithFilter_shouldApplyNameFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(Collections.singletonList(item));

        when(itemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(itemPage);
        when(itemMapper.toItemResponsesPage(itemPage)).thenReturn(new PageImpl<>(Collections.singletonList(itemResponse)));

        // when
        Page<ItemResponse> result = itemService.getItemsWithFilter(null, "Test", null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getItemsWithFilter_shouldApplyPriceFilter() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Item> itemPage = new PageImpl<>(Collections.singletonList(item));

        when(itemRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(itemPage);
        when(itemMapper.toItemResponsesPage(itemPage)).thenReturn(new PageImpl<>(Collections.singletonList(itemResponse)));

        // when
        Page<ItemResponse> result = itemService.getItemsWithFilter(null, null, new BigDecimal("50.00"), pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
    }
}
