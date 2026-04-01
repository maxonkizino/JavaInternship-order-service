package com.javainternshiporderservice.mapper;

import org.mapstruct.Mapper;
import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.dto.response.OrderResponse;
import com.javainternshiporderservice.dto.request.create.CreateOrderRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;
import java.util.List;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderResponse toOrderResponse(Order order);

    Order toOrder(CreateOrderRequest createOrderRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrder(UpdateOrderRequest updateOrderRequest, @MappingTarget Order order);
   
    List<OrderResponse> toOrderResponses(List<Order> orders);
   
    Page<OrderResponse> toOrderResponses(Page<Order> orders);

}
