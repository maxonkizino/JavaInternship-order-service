package com.javainternshiporderservice.event;

import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentEventConsumer consumer;

    private UUID orderId;
    private Order order;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        order = new Order();
        order.setId(orderId);
        order.setStatus("active");
    }

    @Test
    void onCreatePaymentEvent_shouldUpdateOrderStatusToPaidForSucceededPayment() {
        CreatePaymentEvent event = event(orderId, "SUCCEEDED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        consumer.onCreatePaymentEvent(event);

        ArgumentCaptor<Order> savedOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(savedOrderCaptor.capture());
        assertEquals("PAID", savedOrderCaptor.getValue().getStatus());
    }

    @Test
    void onCreatePaymentEvent_shouldUpdateOrderStatusToPaymentFailedForCancelledPayment() {
        CreatePaymentEvent event = event(orderId, "CANCELLED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        consumer.onCreatePaymentEvent(event);

        verify(orderRepository).save(order);
        assertEquals("PAYMENT_FAILED", order.getStatus());
    }

    @Test
    void onCreatePaymentEvent_shouldUpdateOrderStatusToUnknownForBlankStatus() {
        CreatePaymentEvent event = event(orderId, " ");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        consumer.onCreatePaymentEvent(event);

        verify(orderRepository).save(order);
        assertEquals("PAYMENT_UNKNOWN", order.getStatus());
    }

    @Test
    void onCreatePaymentEvent_shouldDoNothingWhenOrderNotFound() {
        CreatePaymentEvent event = event(orderId, "CREATED");
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        consumer.onCreatePaymentEvent(event);

        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any(Order.class));
    }

    @Test
    void onCreatePaymentEvent_shouldDoNothingWhenEventOrOrderIdIsNull() {
        consumer.onCreatePaymentEvent(null);
        consumer.onCreatePaymentEvent(event(null, "CREATED"));

        verify(orderRepository, never()).findById(org.mockito.ArgumentMatchers.any());
        verify(orderRepository, never()).save(org.mockito.ArgumentMatchers.any(Order.class));
    }

    private static CreatePaymentEvent event(UUID orderId, String status) {
        CreatePaymentEvent event = new CreatePaymentEvent();
        event.setPaymentId(UUID.randomUUID());
        event.setOrderId(orderId);
        event.setStatus(status);
        return event;
    }
}
