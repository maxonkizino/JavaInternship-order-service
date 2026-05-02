# Integration Tests with WireMock

This directory contains integration tests that use WireMock to simulate the User Service.

## Test Files

### 1. `OrderControllerIntegrationTest.java`
Full end-to-end tests with:
- TestContainers PostgreSQL for database
- WireMock for User Service simulation
- Spring MockMvc for HTTP testing

**Test Scenarios:**
- Create order with successful User Service call
- Get order by ID (owner validation)
- Get all orders (USER sees only own orders, ADMIN sees all)
- Filter orders with active status
- Circuit Breaker fallback when User Service is down

### 2. `OrderServiceWireMockTest.java`
Direct service layer tests with WireMock:
- Feign client communication
- Circuit Breaker behavior
- Retry scenarios
- Fallback responses

### 3. `WireMockConfigurationTest.java`
Basic WireMock setup verification:
- Mock server starts correctly
- Request/response matching
- Error simulation (500, 404, timeout)

## Running Tests

```bash
# All integration tests
mvn test -Dtest="*IntegrationTest,*WireMockTest"

# Specific test class
mvn test -Dtest=OrderControllerIntegrationTest

# With coverage report
mvn test jacoco:report
```

## WireMock Setup

WireMock is auto-configured via `@AutoConfigureWireMock(port = 0)` which starts on a random port.

The User Service URL is dynamically configured:
```java
@DynamicPropertySource
static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("USER_SERVICE_URL", () -> "http://localhost:${wiremock.server.port}");
}
```

## Stubbing Examples

### Success Response
```java
stubFor(get(urlEqualTo("/api/users/1"))
    .willReturn(aResponse()
        .withStatus(200)
        .withHeader("Content-Type", "application/json")
        .withBody("{\"id\": 1, \"name\": \"John\"}")));
```

### Error Response (for Circuit Breaker)
```java
stubFor(get(urlEqualTo("/api/users/1"))
    .willReturn(aResponse()
        .withStatus(500)
        .withBody("Service Unavailable")));
```

### Timeout (for Circuit Breaker)
```java
stubFor(get(urlEqualTo("/api/users/1"))
    .willReturn(aResponse()
        .withStatus(200)
        .withFixedDelay(10000) // 10 seconds
        .withBody("{}")));
```

## Verification

Verify requests were made:
```java
// Single request
verify(getRequestedFor(urlEqualTo("/api/users/1")));

// Multiple requests
verify(3, getRequestedFor(urlEqualTo("/api/users/1")));

// With header
verify(getRequestedFor(urlEqualTo("/api/users/1"))
    .withHeader("Authorization", containing("Bearer")));
```

## Benefits of WireMock

1. **No Real User Service Needed** - Tests run in isolation
2. **Fast** - No network calls to external service
3. **Reliable** - No flaky tests due to external service downtime
4. **Test Edge Cases** - Easily simulate errors, timeouts, delays
5. **CI/CD Friendly** - No dependencies on running services
