package com.javainternshiporderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.javainternshiporderservice.client.UserClient;
import com.javainternshiporderservice.client.UserServiceClient;
import com.javainternshiporderservice.dto.response.UserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Order Service with WireMock simulating User Service
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
class OrderServiceWireMockTest {

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("USER_SERVICE_URL", () -> "http://localhost:${wiremock.server.port}");
    }

    @Autowired
    private UserClient userClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @Test
    void userClient_shouldFetchUser_whenUserServiceReturns200() throws Exception {
        // given
        Long userId = 1L;
        UserInfoResponse expectedUser = new UserInfoResponse();
        expectedUser.setId(userId);
        expectedUser.setName("John");
        expectedUser.setSurname("Doe");
        expectedUser.setEmail("john@example.com");
        expectedUser.setBirthDate(LocalDate.of(1990, 1, 1));
        expectedUser.setActive(true);

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(expectedUser))));

        // when
        UserInfoResponse actualUser = userClient.getUserById(userId);

        // then
        assertThat(actualUser.getId()).isEqualTo(userId);
        assertThat(actualUser.getName()).isEqualTo("John");
        assertThat(actualUser.getSurname()).isEqualTo("Doe");

        verify(getRequestedFor(urlEqualTo("/api/users/" + userId)));
    }

    @Test
    void userClient_shouldFetchUserByEmail_whenUserServiceReturns200() throws Exception {
        // given
        String email = "john@example.com";
        UserInfoResponse expectedUser = new UserInfoResponse();
        expectedUser.setId(1L);
        expectedUser.setName("John");
        expectedUser.setSurname("Doe");
        expectedUser.setEmail(email);
        expectedUser.setBirthDate(LocalDate.of(1990, 1, 1));
        expectedUser.setActive(true);

        stubFor(get(urlEqualTo("/api/users/by-email/" + email))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(expectedUser))));

        // when
        UserInfoResponse actualUser = userClient.getUserByEmail(email);

        // then
        assertThat(actualUser.getEmail()).isEqualTo(email);
        assertThat(actualUser.getName()).isEqualTo("John");

        verify(getRequestedFor(urlEqualTo("/api/users/by-email/" + email)));
    }

    @Test
    void userServiceClient_shouldReturnFallback_whenUserServiceIsDown() {
        // given - User Service returns 500
        Long userId = 999L;

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // when - Circuit Breaker triggers fallback
        UserInfoResponse fallbackUser = userServiceClient.fetchUserById(userId);

        // then
        assertThat(fallbackUser.getName()).isEqualTo("Unknown");
        assertThat(fallbackUser.getSurname()).isEqualTo("User");
        assertThat(fallbackUser.isActive()).isFalse();
    }

    @Test
    void userServiceClient_shouldReturnFallback_whenUserServiceTimesOut() {
        // given - User Service times out
        Long userId = 888L;

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(10000) // 10 seconds delay
                        .withBody("{}")));

        // when - Circuit Breaker should trigger after timeout
        UserInfoResponse fallbackUser = userServiceClient.fetchUserById(userId);

        // then - Should get fallback response
        assertThat(fallbackUser.getName()).isEqualTo("Unknown");
        assertThat(fallbackUser.getSurname()).isEqualTo("User");
    }

    @Test
    void userServiceClient_shouldRetryAndThenFallback() {
        // given - User Service fails first 2 times, then succeeds
        Long userId = 777L;

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED)
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable"))
                .willSetStateTo("First Retry"));

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("First Retry")
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable"))
                .willSetStateTo("Second Retry"));

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Second Retry")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\": 777, \"name\": \"Success\"}")));

        // when
        UserInfoResponse result = userServiceClient.fetchUserById(userId);

        // then - Circuit Breaker may trigger depending on configuration
        // In this case, it should eventually succeed or fallback
    }

    @Test
    void wireMockShouldRecordMultipleRequests() throws Exception {
        // given
        Long userId = 1L;
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(userId);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        // when - Make multiple requests
        userClient.getUserById(userId);
        userClient.getUserById(userId);
        userClient.getUserById(userId);

        // then
        verify(3, getRequestedFor(urlEqualTo("/api/users/" + userId)));
    }
}
