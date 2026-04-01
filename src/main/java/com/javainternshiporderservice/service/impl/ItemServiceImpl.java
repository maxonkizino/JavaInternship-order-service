package com.javainternshiporderservice.service.impl;

import org.springframework.stereotype.Service;
import com.javainternshiporderservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import com.javainternshiporderservice.repository.ItemRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import com.javainternshiporderservice.mapper.ItemMapper;
import com.javainternshiporderservice.model.Item;
import com.javainternshiporderservice.model.specification.ItemSpecification;
import com.javainternshiporderservice.exception.ItemNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final String ITEM_NOT_FOUND_MESSAGE = "Item not found";



    private final ItemMapper itemMapper;
    private final ItemRepository itemRepository;


    @Override
    public ItemResponse getItemById(UUID id) {
        Specification<Item> spec = Specification
            .where(ItemSpecification.hasId(id))
            .and(ItemSpecification.isActive());
        
        Item item = itemRepository
                .findOne(spec)
                .orElseThrow(() -> new ItemNotFoundException(ITEM_NOT_FOUND_MESSAGE + " with id: " + id));

        return itemMapper.toItemResponse(item);
    }

    @Override
    public ItemResponse getItemByName(String name) {
        Specification<Item> spec = Specification
            .where(ItemSpecification.hasName(name))
            .and(ItemSpecification.isActive());

        Item item = itemRepository
                .findOne(spec)
                .orElseThrow(() -> new ItemNotFoundException(ITEM_NOT_FOUND_MESSAGE + " with name: " + name));
    
        return itemMapper.toItemResponse(item);
    }

    @Override
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        Specification<Item> spec = Specification
            .where(ItemSpecification.isActive());

        Page<Item> items = itemRepository.findAll(spec, pageable);

        return itemMapper.toItemResponsesPage(items);
    }

    @Override
    public Page<ItemResponse> getItemsWithFilter(
            Boolean active,
            String name,
            BigDecimal price,
            Pageable pageable) {

        Specification<Item> spec = (root, query, cb) -> null;

        if (active != null) {
            spec = spec.and(ItemSpecification.hasActive(active));
        }
        if (name != null) {
            spec = spec.and(ItemSpecification.hasName(name));
        }
        if (price != null) {
            spec = spec.and(ItemSpecification.hasPrice(price));
        }

        Page<Item> items = itemRepository.findAll(spec, pageable);
        return itemMapper.toItemResponsesPage(items);
    }

    @Override
    @Transactional
    public ItemResponse createItem(CreateItemRequest createItemRequest) {
        Item item = itemMapper.toItem(createItemRequest);
        Item createdItem = itemRepository.save(item);
        return itemMapper.toItemResponse(createdItem);

    }       

    @Override
    @Transactional
    public ItemResponse updateItem(UpdateItemRequest updateItemRequest) {
        Item item = itemRepository
            .findById(updateItemRequest.getId())
            .orElseThrow(() -> new ItemNotFoundException(ITEM_NOT_FOUND_MESSAGE + " with id: " + updateItemRequest.getId()));
        itemMapper.updateItem(updateItemRequest, item);
        Item updatedItem = itemRepository.save(item);
        return itemMapper.toItemResponse(updatedItem);
    }

    @Override
    @Transactional
    public void activateItem(UUID id) {
        itemRepository.activateItem(id);
    }

    @Override
    @Transactional
    public void deactivateItem(UUID id) {
        itemRepository.deactivateItem(id);
    }

}
