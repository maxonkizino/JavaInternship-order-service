package com.javainternshiporderservice.client;

import com.javainternshiporderservice.dto.response.UserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;

import java.time.LocalDate;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceClientTest {

    private static final String CB_NAME = "userService";

    @Mock
    private UserClient userClient;

    @Mock
    private Resilience4JCircuitBreakerFactory circuitBreakerFactory;

    @Mock
    private UserServiceFallback fallback;

    @Mock
    private CircuitBreaker circuitBreaker;

    @InjectMocks
    private UserServiceClient userServiceClient;

    @BeforeEach
    void setUpCircuitBreaker() {
        when(circuitBreakerFactory.create(CB_NAME)).thenReturn(circuitBreaker);
        when(circuitBreaker.run(
                ArgumentMatchers.<Supplier<UserInfoResponse>>any(),
                ArgumentMatchers.<Function<Throwable, UserInfoResponse>>any()))
                .thenAnswer(invocation -> {
            Supplier<UserInfoResponse> supplier = invocation.getArgument(0);
            Function<Throwable, UserInfoResponse> fallbackFn = invocation.getArgument(1);
            try {
                return supplier.get();
            } catch (Throwable t) {
                return fallbackFn.apply(t);
            }
        });
    }

    @Test
    void fetchUserById_shouldReturnUserFromClient() {
        Long userId = 1L;
        UserInfoResponse expected = buildUser(userId);
        when(userClient.getUserById(userId)).thenReturn(expected);

        UserInfoResponse result = userServiceClient.fetchUserById(userId);

        assertThat(result).isSameAs(expected);
        verify(userClient).getUserById(userId);
    }

    @Test
    void fetchUserById_shouldReturnFallback_whenClientThrows() {
        Long userId = 2L;
        UserInfoResponse fallbackUser = new UserInfoResponse();
        fallbackUser.setName("Unknown");
        when(userClient.getUserById(userId)).thenThrow(new RuntimeException("service down"));
        when(fallback.createFallback("userId=" + userId)).thenReturn(fallbackUser);

        UserInfoResponse result = userServiceClient.fetchUserById(userId);

        assertThat(result).isSameAs(fallbackUser);
        verify(fallback).createFallback("userId=" + userId);
    }

    @Test
    void fetchUserByEmail_shouldReturnUserFromClient() {
        String email = "a@b.com";
        UserInfoResponse expected = buildUser(10L);
        expected.setEmail(email);
        when(userClient.getUserByEmail(email)).thenReturn(expected);

        UserInfoResponse result = userServiceClient.fetchUserByEmail(email);

        assertThat(result).isSameAs(expected);
        verify(userClient).getUserByEmail(email);
    }

    @Test
    void fetchUserByEmail_shouldReturnFallback_whenClientThrows() {
        String email = "x@y.com";
        UserInfoResponse fallbackUser = new UserInfoResponse();
        when(userClient.getUserByEmail(email)).thenThrow(new RuntimeException("timeout"));
        when(fallback.createFallback("email=" + email)).thenReturn(fallbackUser);

        UserInfoResponse result = userServiceClient.fetchUserByEmail(email);

        assertThat(result).isSameAs(fallbackUser);
        verify(fallback).createFallback("email=" + email);
    }

    private static UserInfoResponse buildUser(Long id) {
        UserInfoResponse u = new UserInfoResponse();
        u.setId(id);
        u.setName("John");
        u.setSurname("Doe");
        u.setEmail("john@example.com");
        u.setBirthDate(LocalDate.of(1990, 1, 1));
        u.setActive(true);
        return u;
    }
}
