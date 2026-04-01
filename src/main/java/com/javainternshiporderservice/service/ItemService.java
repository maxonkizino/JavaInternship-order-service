package com.javainternshiporderservice.service;

import org.springframework.stereotype.Service;

import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service

public interface ItemService {


    ItemResponse getItemById(UUID id);

    ItemResponse getItemByName(String name);

    Page<ItemResponse> getAllItems(Pageable pageable);

    






    void createItem(CreateItemRequest createItemRequest);

    void updateItem(UpdateItemRequest updateItemRequest);

    void activateItem(UUID id);

    void deactivateItem(UUID id);


    
}
