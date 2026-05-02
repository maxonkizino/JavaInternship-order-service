package com.javainternshiporderservice.client;

import com.javainternshiporderservice.dto.response.UserInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClient {

    private static final String USER_SERVICE_CB = "userService";

    private final UserClient userClient;
    private final Resilience4JCircuitBreakerFactory circuitBreakerFactory;
    private final UserServiceFallback fallback;

    public UserInfoResponse fetchUserById(Long userId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(USER_SERVICE_CB);

        Supplier<UserInfoResponse> supplier = () -> {
            log.info("Calling User Service for userId: {}", userId);
            return userClient.getUserById(userId);
        };

        return circuitBreaker.run(supplier, throwable -> fallback.createFallback("userId=" + userId));
    }

    public UserInfoResponse fetchUserByEmail(String email) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(USER_SERVICE_CB);

        Supplier<UserInfoResponse> supplier = () -> {
            log.info("Calling User Service for email: {}", email);
            return userClient.getUserByEmail(email);
        };

        return circuitBreaker.run(supplier, throwable -> fallback.createFallback("email=" + email));
    }

}
