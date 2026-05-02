package com.javainternshiporderservice.mapper;

import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import com.javainternshiporderservice.model.OrderItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

/**
 * MapStruct mapper for converting between OrderItem entity and DTOs.
 * Component model "spring" enables Spring dependency injection.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    /**
     * Converts OrderItem entity to OrderItemResponse DTO.
     *
     * @param orderItem the order item entity
     * @return the order item response DTO
     */
    @Mapping(source = "id", target = "id")
    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "item.id", target = "itemId")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    /**
     * Converts CreateOrderItemRequest DTO to OrderItem entity.
     *
     * @param createOrderItemRequest the create order item request DTO
     * @return the order item entity
     */
    OrderItem toOrderItem(CreateOrderItemRequest createOrderItemRequest);

    /**
     * Updates OrderItem entity from UpdateOrderItemRequest DTO.
     * Null values in the request are ignored (partial update).
     *
     * @param updateOrderItemRequest the update order item request DTO
     * @param orderItem the order item entity to update (MappingTarget)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrderItem(UpdateOrderItemRequest updateOrderItemRequest, @MappingTarget OrderItem orderItem);

    /**
     * Converts list of OrderItem entities to list of OrderItemResponse DTOs.
     *
     * @param orderItems the list of order item entities
     * @return the list of order item response DTOs
     */
    List<OrderItemResponse> toOrderItemResponses(List<OrderItem> orderItems);

    /**
     * Converts Page of OrderItem entities to Page of OrderItemResponse DTOs.
     * Default implementation to handle Page mapping.
     *
     * @param orderItems the page of order item entities
     * @return the page of order item response DTOs
     */
    default Page<OrderItemResponse> toOrderItemResponsesPage(Page<OrderItem> orderItems) {
        if (orderItems == null) {
            return null;
        }
        List<OrderItemResponse> content = orderItems.getContent().stream()
                .map(this::toOrderItemResponse)
                .toList();
        return new PageImpl<>(content, orderItems.getPageable(), orderItems.getTotalElements());
    }

}
