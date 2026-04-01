package com.javainternshiporderservice.service.impl;

import org.springframework.stereotype.Service;
import com.javainternshiporderservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import com.javainternshiporderservice.repository.ItemRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

private final ItemRepository itemRepository;


@Override
public ItemResponse getItemById(UUID id) {
    return itemRepository.findById(id);
}

@Override
public ItemResponse getItemByName(String name) {
    return itemRepository.findByName(name);
}

@Override
public Page<ItemResponse> getAllItems(Pageable pageable) {
    return itemRepository.findAll(pageable);
}

@Override
public void createItem(CreateItemRequest createItemRequest) {
    itemRepository.createItem(createItemRequest);
}       

@Override
public void updateItem(UpdateItemRequest updateItemRequest) {
    itemRepository.updateItem(updateItemRequest);
}

@Override
public void activateItem(UUID id) {
    itemRepository.activateItem(id);
}

@Override
public void deactivateItem(UUID id) {
    itemRepository.deactivateItem(id);
}

}
