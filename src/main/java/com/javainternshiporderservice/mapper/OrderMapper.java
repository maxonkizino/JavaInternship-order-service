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

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toOrderResponse(Order order);

    Order toOrder(CreateOrderRequest createOrderRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrder(UpdateOrderRequest updateOrderRequest, @MappingTarget Order order);

    List<OrderResponse> toOrderResponses(List<Order> orders);

    default Page<OrderResponse> toOrderResponsesPage(Page<Order> orders) {
        if (orders == null) {
            return null;
        }
        List<OrderResponse> content = orders.getContent().stream()
                .map(this::toOrderResponse)
                .toList();
        return new PageImpl<>(content, orders.getPageable(), orders.getTotalElements());
    }

}
