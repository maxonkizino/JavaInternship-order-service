package com.javainternshiporderservice.mapper;

import org.mapstruct.Mapper;
import com.javainternshiporderservice.model.OrderItem;
import com.javainternshiporderservice.dto.response.OrderItemResponse;
import com.javainternshiporderservice.dto.request.create.CreateOrderItemRequest;
import com.javainternshiporderservice.dto.request.update.UpdateOrderItemRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.MappingTarget;
import java.util.List;
import org.springframework.data.domain.Page;
@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);
   
    OrderItem toOrderItem(CreateOrderItemRequest createOrderItemRequest);
   
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateOrderItem(UpdateOrderItemRequest updateOrderItemRequest, @MappingTarget OrderItem orderItem);
   
    List<OrderItemResponse> toOrderItemResponses(List<OrderItem> orderItems);
   
    Page<OrderItemResponse> toOrderItemResponses(Page<OrderItem> orderItems);

}
