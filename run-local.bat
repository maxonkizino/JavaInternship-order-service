@echo off
echo Starting Order Service with LOCAL profile (H2 Database)...
echo.
echo Features:
echo - H2 In-Memory Database (no PostgreSQL needed)
echo - JWT Disabled (no authentication required)
echo - H2 Console: http://localhost:8080/h2-console
necho.
set SPRING_PROFILES_ACTIVE=local
.\mvnw spring-boot:run
