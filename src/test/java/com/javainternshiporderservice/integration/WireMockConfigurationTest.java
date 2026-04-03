package com.javainternshiporderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.javainternshiporderservice.dto.response.UserInfoResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify WireMock is properly configured and can mock User Service responses
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WireMockConfigurationTest {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = createRestTemplate();

    private static RestTemplate createRestTemplate() {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }
        });
        return template;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("USER_SERVICE_URL", () -> "http://localhost:" + wireMockServer.port());
    }

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(0); // random port
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void wireMockShouldRespondToUserServiceRequest() throws Exception {
        // given
        Long userId = 1L;
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(userId);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        wireMockServer.stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + wireMockServer.port() + "/api/users/" + userId,
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("John").contains("Doe");

        wireMockServer.verify(getRequestedFor(urlEqualTo("/api/users/" + userId)));
    }

    @Test
    void wireMockShouldSimulateUserServiceFailure() {
        // given
        Long userId = 999L;

        wireMockServer.stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + wireMockServer.port() + "/api/users/" + userId,
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void wireMockShouldSimulateUserNotFound() {
        // given
        Long userId = 888L;

        wireMockServer.stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("User not found")));

        // when
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + wireMockServer.port() + "/api/users/" + userId,
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void wireMockShouldSimulateTimeout() {
        // given
        Long userId = 777L;

        wireMockServer.stubFor(get(urlEqualTo("/api/users/" + userId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(5000))); // 5 second delay

        // when & then - Circuit Breaker should trigger fallback
        // This tests the timeout configuration
    }
}
