@echo off
start "User Service" /D "user-service" mvn spring-boot:run
start "Workout Service" /D "workout-service" mvn spring-boot:run
start "Booking Service" /D "booking-service" mvn spring-boot:run
start "Analytics Service" /D "analytics-service" mvn spring-boot:run
start "API Gateway" /D "api-gateway" mvn spring-boot:run
