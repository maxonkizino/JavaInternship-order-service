package com.javainternshiporderservice.event;

import com.javainternshiporderservice.model.Order;
import com.javainternshiporderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;

    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.create-payment}")
    public void onCreatePaymentEvent(CreatePaymentEvent event) {
        if (event == null || event.getOrderId() == null) {
            return;
        }

        orderRepository.findById(event.getOrderId()).ifPresentOrElse(order -> {
            String newStatus = mapOrderStatus(event.getStatus());
            order.setStatus(newStatus);
            orderRepository.save(order);
            log.info("Order {} status updated to {} from create-payment event", order.getId(), newStatus);
        }, () -> log.warn("Order {} not found for create-payment event", event.getOrderId()));
    }

    private String mapOrderStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) {
            return "PAYMENT_UNKNOWN";
        }

        return switch (paymentStatus.toUpperCase(Locale.ROOT)) {
            case "SUCCEEDED" -> "PAID";
            case "FAILED", "CANCELLED" -> "PAYMENT_FAILED";
            case "PROCESSING" -> "PAYMENT_PROCESSING";
            case "CREATED" -> "PAYMENT_CREATED";
            default -> "PAYMENT_UNKNOWN";
        };
    }
}
