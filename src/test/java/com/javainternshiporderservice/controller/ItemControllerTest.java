package com.javainternshiporderservice.controller;

import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.service.ItemService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private UUID itemId;
    private ItemResponse itemResponse;
    private CreateItemRequest createItemRequest;
    private UpdateItemRequest updateItemRequest;

    @BeforeEach
    void setUp() {
        itemId = UUID.randomUUID();
        itemResponse = new ItemResponse();
        itemResponse.setId(itemId);
        itemResponse.setName("Test Item");
        itemResponse.setPrice(new BigDecimal("99.99"));
        itemResponse.setActive(true);

        createItemRequest = new CreateItemRequest();
        createItemRequest.setName("New Item");
        createItemRequest.setPrice(new BigDecimal("49.99"));

        updateItemRequest = new UpdateItemRequest();
        updateItemRequest.setId(itemId);
        updateItemRequest.setName("Updated Item");
        updateItemRequest.setPrice(new BigDecimal("79.99"));
    }

    @Test
    void getItemById_shouldReturnItem() {
        when(itemService.getItemById(itemId)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> response = itemController.getItemById(itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(itemResponse);
        verify(itemService).getItemById(itemId);
    }

    @Test
    void getAllItems_shouldReturnPageOfItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemResponse> page = new PageImpl<>(Collections.singletonList(itemResponse));
        when(itemService.getAllItems(pageable)).thenReturn(page);

        ResponseEntity<Page<ItemResponse>> response = itemController.getAllItems(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        assertThat(response.getBody().getContent()).hasSize(1);
        verify(itemService).getAllItems(pageable);
    }

    @Test
    void getItemsWithFilter_shouldReturnFilteredItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemResponse> page = new PageImpl<>(Collections.singletonList(itemResponse));
        when(itemService.getItemsWithFilter(true, "Test", new BigDecimal("99.99"), pageable)).thenReturn(page);

        ResponseEntity<Page<ItemResponse>> response = itemController.getItemsWithFilter(true, "Test", new BigDecimal("99.99"), pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(page);
        verify(itemService).getItemsWithFilter(true, "Test", new BigDecimal("99.99"), pageable);
    }

    @Test
    void getItemsWithFilter_shouldWorkWithNullParameters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemResponse> page = new PageImpl<>(Collections.singletonList(itemResponse));
        when(itemService.getItemsWithFilter(null, null, null, pageable)).thenReturn(page);

        ResponseEntity<Page<ItemResponse>> response = itemController.getItemsWithFilter(null, null, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(itemService).getItemsWithFilter(null, null, null, pageable);
    }

    @Test
    void createItem_shouldCreateAndReturnItem() {
        when(itemService.createItem(createItemRequest)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> response = itemController.createItem(createItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(itemResponse);
        verify(itemService).createItem(createItemRequest);
    }

    @Test
    void updateItem_shouldUpdateAndReturnItem() {
        when(itemService.updateItem(updateItemRequest)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> response = itemController.updateItem(updateItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(itemResponse);
        verify(itemService).updateItem(updateItemRequest);
    }

    @Test
    void activateItem_shouldActivateItem() {
        doNothing().when(itemService).activateItem(itemId);

        ResponseEntity<Void> response = itemController.activateItem(itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(itemService).activateItem(itemId);
    }

    @Test
    void deactivateItem_shouldDeactivateItem() {
        doNothing().when(itemService).deactivateItem(itemId);

        ResponseEntity<Void> response = itemController.deactivateItem(itemId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(itemService).deactivateItem(itemId);
    }

    @Test
    void getAllItems_shouldReturnEmptyPage_whenNoItems() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemResponse> emptyPage = new PageImpl<>(Collections.emptyList());
        when(itemService.getAllItems(pageable)).thenReturn(emptyPage);

        ResponseEntity<Page<ItemResponse>> response = itemController.getAllItems(pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getContent()).isEmpty();
        verify(itemService).getAllItems(pageable);
    }

    @Test
    void getItemsWithFilter_shouldHandlePartialFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ItemResponse> page = new PageImpl<>(Collections.singletonList(itemResponse));
        when(itemService.getItemsWithFilter(true, null, null, pageable)).thenReturn(page);

        ResponseEntity<Page<ItemResponse>> response = itemController.getItemsWithFilter(true, null, null, pageable);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(itemService).getItemsWithFilter(true, null, null, pageable);
    }

    @Test
    void createItem_shouldHandleInactiveItem() {
        createItemRequest.setActive(false);
        itemResponse.setActive(false);
        when(itemService.createItem(createItemRequest)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> response = itemController.createItem(createItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().isActive()).isFalse();
    }

    @Test
    void updateItem_shouldHandlePriceUpdate() {
        updateItemRequest.setPrice(new BigDecimal("150.00"));
        itemResponse.setPrice(new BigDecimal("150.00"));
        when(itemService.updateItem(updateItemRequest)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> response = itemController.updateItem(updateItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    void updateItem_shouldHandleNameUpdate() {
        updateItemRequest.setName("New Item Name");
        itemResponse.setName("New Item Name");
        when(itemService.updateItem(updateItemRequest)).thenReturn(itemResponse);

        ResponseEntity<ItemResponse> response = itemController.updateItem(updateItemRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("New Item Name");
    }
}
