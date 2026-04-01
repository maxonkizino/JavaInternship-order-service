package com.javainternshiporderservice.mapper;

import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import com.javainternshiporderservice.dto.response.OrderResponse;
import com.javainternshiporderservice.model.Order;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for converting between Order entity and DTOs.
 * Component model "spring" enables Spring dependency injection.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    /**
     * Converts Order entity to OrderResponse DTO.
     *
     * @param order the order entity
     * @return the order response DTO
     */
    OrderResponse toOrderResponse(Order order);

    /**
     * Converts CreateOrderRequest DTO to Order entity.
     *
     * @param createOrderRequest the create order request DTO
     * @return the order entity
     */
    Order toOrder(CreateOrderRequest createOrderRequest);

    /**
     * Updates Order entity from UpdateOrderRequest DTO.
     * Null values in the request are ignored (partial update).
     *
     * @param updateOrderRequest the update order request DTO
     * @param order the order entity to update (MappingTarget)
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrder(UpdateOrderRequest updateOrderRequest, @MappingTarget Order order);

    /**
     * Converts list of Order entities to list of OrderResponse DTOs.
     *
     * @param orders the list of order entities
     * @return the list of order response DTOs
     */
    List<OrderResponse> toOrderResponses(List<Order> orders);

    /**
     * Converts Page of Order entities to Page of OrderResponse DTOs.
     * Default implementation to handle Page mapping.
     *
     * @param orders the page of order entities
     * @return the page of order response DTOs
     */
    default Page<OrderResponse> toOrderResponsesPage(Page<Order> orders) {
        if (orders == null) {
            return null;
        }
        List<OrderResponse> content = orders.getContent().stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
        return new PageImpl<>(content, orders.getPageable(), orders.getTotalElements());
    }

}
