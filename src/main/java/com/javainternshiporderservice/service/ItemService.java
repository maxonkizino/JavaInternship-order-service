package com.javainternshiporderservice.service;

import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import com.javainternshiporderservice.dto.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service interface for managing items (products).
 * Provides CRUD operations and status management for items.
 */
@Service
public interface ItemService {

    /**
     * Retrieves an item by its ID.
     *
     * @param id the UUID of the item
     * @return ItemResponse containing item details
     * @throws com.javainternshiporderservice.exception.ItemNotFoundException if item not found
     */
    ItemResponse getItemById(UUID id);

    /**
     * Retrieves an item by its name.
     *
     * @param name the name of the item
     * @return ItemResponse containing item details
     * @throws com.javainternshiporderservice.exception.ItemNotFoundException if item not found
     */
    ItemResponse getItemByName(String name);

    /**
     * Retrieves all active items with pagination.
     *
     * @param pageable pagination information
     * @return Page of ItemResponse
     */
    Page<ItemResponse> getAllItems(Pageable pageable);

    /**
     * Creates a new item.
     *
     * @param createItemRequest the item creation request
     * @return ItemResponse of the created item
     */
    ItemResponse createItem(CreateItemRequest createItemRequest);

    /**
     * Updates an existing item.
     *
     * @param updateItemRequest the item update request
     * @return ItemResponse of the updated item
     * @throws com.javainternshiporderservice.exception.ItemNotFoundException if item not found
     */
    ItemResponse updateItem(UpdateItemRequest updateItemRequest);

    /**
     * Activates (soft restore) an item by ID.
     *
     * @param id the UUID of the item
     */
    void activateItem(UUID id);

    /**
     * Deactivates (soft delete) an item by ID.
     *
     * @param id the UUID of the item
     */
    void deactivateItem(UUID id);
}
