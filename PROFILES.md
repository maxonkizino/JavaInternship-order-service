# Spring Profiles Configuration

This document describes the Spring profiles available in the Order Service.

## Available Profiles

### 1. `local` (Default)
**Purpose:** Quick local development with minimal setup

**Features:**
- H2 in-memory database (no PostgreSQL needed)
- JWT security disabled (all endpoints permit all)
- SQL logging enabled
- H2 console at `/h2-console`
- Circuit Breaker disabled
- Detailed logging

**Usage:**
```bash
./mvnw spring-boot:run
# or explicitly:
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

---

### 2. `dev`
**Purpose:** Development with external services

**Features:**
- PostgreSQL on localhost:5432
- JWT enabled (local auth service)
- SQL logging enabled
- Debug logging for security
- All actuator endpoints exposed
- Relaxed Circuit Breaker settings

**Prerequisites:**
- PostgreSQL running on localhost
- User Service running on localhost:8082
- Auth Service running on localhost:8081

**Usage:**
```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

**Environment Variables:**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/order_service_dev
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export USER_SERVICE_URL=http://localhost:8082
export JWT_ISSUER_URI=http://localhost:8081
```

---

### 3. `test`
**Purpose:** Integration tests

**Features:**
- TestContainers PostgreSQL (Docker container)
- WireMock for User Service mocking
- JWT disabled (using @WithMockUser)
- Aggressive Circuit Breaker timeouts for fast tests
- Random server port for parallel tests
- Liquibase `drop-first: true`

**Usage:**
```bash
./mvnw test
# or explicitly:
SPRING_PROFILES_ACTIVE=test ./mvnw test
```

**Note:** Docker must be running for TestContainers to work.

---

### 4. `prod`
**Purpose:** Production deployment

**Features:**
- External database from environment variables
- Connection pooling (HikariCP)
- JWT enabled (production auth service)
- Minimal logging (WARN and above)
- Strict security settings
- File logging to `/var/log/order-service/`
- Limited actuator endpoints (health, info, metrics, prometheus)
- Optimized Circuit Breaker settings

**Required Environment Variables:**
```bash
export DB_URL=jdbc:postgresql://prod-db-host:5432/order_service
export DB_USERNAME=order_service_user
export DB_PASSWORD=<secure_password>
export USER_SERVICE_URL=http://user-service.prod.svc.cluster.local
export JWT_ISSUER_URI=https://auth.company.com
export JWT_JWK_SET_URI=https://auth.company.com/.well-known/jwks.json
export ALLOWED_ORIGINS=https://company.com,https://admin.company.com
```

**Usage:**
```bash
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

**Docker:**
```bash
docker run -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://... \
  -e DB_USERNAME=... \
  -e DB_PASSWORD=... \
  order-service:latest
```

---

## Profile Selection Priority

Spring Boot selects profiles in the following order (highest to lowest):

1. `SPRING_PROFILES_ACTIVE` environment variable
2. `spring.profiles.active` in `application.yml`
3. Programmatic set (in code)
4. Default profile (local)

## Combining Profiles

You can activate multiple profiles:

```bash
SPRING_PROFILES_ACTIVE=dev,local-cache ./mvnw spring-boot:run
```

Properties from later profiles override earlier ones.

## Profile-Specific Configuration Files

| File | Purpose |
|------|---------|
| `application.yml` | Common configuration (all profiles) |
| `application-local.yml` | Local development settings |
| `application-dev.yml` | Development environment settings |
| `application-test.yml` | Test environment settings |
| `application-prod.yml` | Production environment settings |

## Checking Active Profile

The application logs active profiles on startup:

```
Application 'order-service' version '1.0.0' started with profile(s): [dev]
```

Or check the `/actuator/info` endpoint when running.

## Common Commands

```bash
# Run locally (H2 database)
./mvnw spring-boot:run

# Run with dev profile (PostgreSQL)
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run

# Run tests
./mvnw test

# Run with production profile (requires external services)
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run

# Build for production
SPRING_PROFILES_ACTIVE=prod ./mvnw clean package

# Build Docker image for production
docker build -t order-service:latest .
docker run -e SPRING_PROFILES_ACTIVE=prod order-service:latest
```

## Security Warning

⚠️ **Never use `local` or `dev` profiles in production!**

- `local` profile disables authentication
- `dev` profile has relaxed security and verbose logging

Always use `prod` profile for production deployments and verify all security settings are in place.

## Troubleshooting

### Profile not loading
```bash
# Check active profiles
curl http://localhost:8080/actuator/info

# Or check logs for:
# "Application started with profile(s): [xxx]"
```

### Database connection fails
- Verify the correct profile is active
- Check environment variables for `dev`/`prod`
- For `local` profile, H2 should auto-configure

### JWT issues
- `local` profile: JWT is disabled (no token needed)
- `dev`/`prod` profiles: Ensure Auth Service is accessible
- Check `JWT_ISSUER_URI` and `JWT_JWK_SET_URI` environment variables
