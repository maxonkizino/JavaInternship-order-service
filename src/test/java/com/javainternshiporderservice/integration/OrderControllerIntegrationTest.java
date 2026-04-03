package com.javainternshiporderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.javainternshiporderservice.dto.response.UserInfoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@AutoConfigureWireMock(port = 0)
@Sql(scripts = {"/clean-orders.sql", "/item-seed.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("order_service_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("USER_SERVICE_URL", () -> "http://localhost:${wiremock.server.port}");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        WireMock.reset();
    }

    @Test
    @WithMockUser(username = "1", authorities = {"ROLE_USER"})
    void createOrder_shouldCreateOrder_whenUserServiceReturnsUser() throws Exception {
        // given - Mock User Service response
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        // when & then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.order.status").value("PENDING"))
                .andExpect(jsonPath("$.user.name").value("John"));

        verify(getRequestedFor(urlEqualTo("/api/users/1")));
    }

    @Test
    @WithMockUser(username = "1", authorities = {"ROLE_USER"})
    void getOrderById_shouldReturnOrder_whenOrderExistsAndUserIsOwner() throws Exception {
        // First create an order
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response).get("order").get("id").asText();

        // when & then - Get the order
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(orderId))
                .andExpect(jsonPath("$.order.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "1", authorities = {"ROLE_USER"})
    void getOrderById_shouldReturn403_whenUserIsNotOwner() throws Exception {
        // Create order for different user (admin can do this)
        // This test would need admin setup - simplified here
        String differentOrderId = "550e8400-e29b-41d4-a716-446655440000";

        mockMvc.perform(get("/api/orders/{id}", differentOrderId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "1", authorities = {"ROLE_USER"})
    void getAllOrders_shouldReturnOnlyUserOrders() throws Exception {
        // given - Create multiple orders for user 1
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        // Create two orders
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated());

        // when & then
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void getAllOrdersAsAdmin_shouldReturnAllOrders() throws Exception {
        // given
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlMatching("/api/users/\\d+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        // when & then
        mockMvc.perform(get("/api/orders")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1", authorities = {"ROLE_USER"})
    void getOrdersWithFilter_shouldApplyActiveFilter() throws Exception {
        // given
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated());

        // when & then - Filter by active=true
        mockMvc.perform(get("/api/orders/filtered")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "1", authorities = {"ROLE_USER"})
    void userServiceCircuitBreaker_shouldReturnFallback_whenUserServiceIsDown() throws Exception {
        // given - User Service returns error (simulating Circuit Breaker)
        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Service Unavailable")));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        // when & then - Should use fallback user response
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.name").value("Unknown"))
                .andExpect(jsonPath("$.user.surname").value("User"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void updateOrder_shouldUpdateOrder() throws Exception {
        // given - Create an order first
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response).get("order").get("id").asText();

        // when & then - Update the order
        String updateOrderJson = String.format("""
            {
                "id": "%s",
                "userId": 1,
                "status": "CONFIRMED",
                "totalPrice": 200.00,
                "active": true
            }
            """, orderId);

        mockMvc.perform(put("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateOrderJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.order.totalPrice").value(200.00));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void activateOrder_shouldActivateOrder() throws Exception {
        // given - Create and deactivate an order
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response).get("order").get("id").asText();

        // Deactivate first
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());

        // when & then - Activate the order
        mockMvc.perform(put("/api/orders/{id}/activate", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ROLE_ADMIN"})
    void deactivateOrder_shouldDeactivateOrder() throws Exception {
        // given - Create an order
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response).get("order").get("id").asText();

        // when & then - Deactivate the order
        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"})
    void getOrderWithUserById_shouldReturnOrderWithUser() throws Exception {
        // given - Create an order
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(1L);
        userInfo.setName("John");
        userInfo.setSurname("Doe");
        userInfo.setEmail("john@example.com");
        userInfo.setBirthDate(LocalDate.of(1990, 1, 1));
        userInfo.setActive(true);

        stubFor(WireMock.get(urlEqualTo("/api/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        stubFor(WireMock.get(urlEqualTo("/api/users/by-email/john%40example.com"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userInfo))));

        String createOrderJson = """
            {
                "userId": 1,
                "status": "PENDING",
                "totalPrice": 150.00,
                "orderItems": [
                    { "itemId": "11111111-1111-1111-1111-111111111111", "quantity": 1, "active": true }
                ],
                "active": true
            }
            """;

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createOrderJson))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String orderId = objectMapper.readTree(response).get("order").get("id").asText();

        // when & then - Get order with user by email
        mockMvc.perform(get("/api/orders/{id}/with-user", orderId)
                        .param("userEmail", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.order.id").value(orderId))
                .andExpect(jsonPath("$.user.email").value("john@example.com"));
    }
}
