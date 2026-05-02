package com.javainternshiporderservice.client;

import com.javainternshiporderservice.dto.response.UserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceFallbackTest {

    private UserServiceFallback fallback;

    @BeforeEach
    void setUp() {
        fallback = new UserServiceFallback();
    }

    @Test
    void createFallback_shouldReturnPlaceholderUser() {
        UserInfoResponse response = fallback.createFallback("userId=5");

        assertThat(response.getName()).isEqualTo("Unknown");
        assertThat(response.getSurname()).isEqualTo("User");
        assertThat(response.isActive()).isFalse();
    }
}
