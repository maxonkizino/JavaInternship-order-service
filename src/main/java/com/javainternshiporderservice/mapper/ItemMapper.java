package com.javainternshiporderservice.mapper;

import com.javainternshiporderservice.dto.request.create.CreateItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateItemRequest;
import com.javainternshiporderservice.dto.response.ItemResponse;
import com.javainternshiporderservice.model.Item;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

/**
 * MapStruct mapper for converting between Item entity and DTOs.
 * Component model "spring" enables Spring dependency injection.
 */
@Mapper(componentModel = "spring")
public interface ItemMapper {

    /**
     * Converts Item entity to ItemResponse DTO.
     *
     * @param item the item entity
     * @return the item response DTO
     */
    ItemResponse toItemResponse(Item item);

    /**
     * Converts CreateItemRequest DTO to Item entity.
     *
     * @param createItemRequest the create item request DTO
     * @return the item entity
     */
    Item toItem(CreateItemRequest createItemRequest);

    /**
     * Updates Item entity from UpdateItemRequest DTO.
     * Null values in the request are ignored (partial update).
     *
     * @param updateItemRequest the update item request DTO
     * @param item the item entity to update (MappingTarget)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateItem(UpdateItemRequest updateItemRequest, @MappingTarget Item item);

    /**
     * Converts list of Item entities to list of ItemResponse DTOs.
     *
     * @param items the list of item entities
     * @return the list of item response DTOs
     */
    List<ItemResponse> toItemResponses(List<Item> items);

    /**
     * Converts Page of Item entities to Page of ItemResponse DTOs.
     * Default implementation to handle Page mapping.
     *
     * @param items the page of item entities
     * @return the page of item response DTOs
     */
    default Page<ItemResponse> toItemResponsesPage(Page<Item> items) {
        if (items == null) {
            return null;
        }
        List<ItemResponse> content = items.getContent().stream()
                .map(this::toItemResponse)
                .toList();
        return new PageImpl<>(content, items.getPageable(), items.getTotalElements());
    }

}
