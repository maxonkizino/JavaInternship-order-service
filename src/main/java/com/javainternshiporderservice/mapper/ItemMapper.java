package com.javainternshiporderservice.mapper;

import org.mapstruct.Mapper;
import com.javainternshiporderservice.model.Item;
import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;
import java.util.List;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemResponse toItemResponse(Item item);

    Item toItem(CreateItemRequest createItemRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateItem(UpdateItemRequest updateItemRequest, @MappingTarget Item item);

    List<ItemResponse> toItemResponses(List<Item> items);

    Page<ItemResponse> toItemResponses(Page<Item> items);
}
