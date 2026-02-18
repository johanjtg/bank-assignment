# Bank Account Opening Assignment

Backend implementation for the ALEXA bank account opening system.

## Overview
This application provides APIs to:
1.  **Onboarding** for an account (Draft mode).
3.  **Pause and Resume** applications (by saving state).
4.  **Submit** final applications with full validation.

## Tech Stack
-   **Language**: Java 17
-   **Framework**: Spring Boot 3.2
-   **Database**: H2 (In-memory)
-   **Documentation**: OpenAPI 3.1.0 (Swagger)
-   **Containerization**: Docker

## Prerequisites
-   Java 17+
-   Maven 3.8+
-   Docker (optional, for containerized run)

## How to Run

### Locally (Maven)
```bash
mvn clean install
mvn spring-boot:run
```
The application will start on port `8080`.

### Using Docker
```bash
docker build -t bank-assignment .
docker run -p 8080:8080 bank-assignment
```

## API Documentation
Once the application is running, you can access the Swagger UI and OpenAPI spec:
-   **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
-   **OpenAPI Spec (YAML)**: [http://localhost:8080/v3/api-docs.yaml](http://localhost:8080/v3/api-docs.yaml)

## Features & Design Decisions

### 1. Immediate Validation and save the application in Draft mode 
The `POST /applications/

### 2. Pause & Resume
The `PATCH /applications/{id}` endpoint runs validation on the updated fields. If a field format is invalid (e.g., PostCode regex), it returns a `400 Bad Request` with specific field errors immediately.
-   **Pause**: Occurs implicitly. Every valid `PATCH` request persists the data to the database.
-   **Resume**: Call `GET /applications/{id}` to retrieve the current state of the application.

### 3. Submission
The `POST /applications/{id}/submit` endpoint performs a final "completeness" check. It ensures all mandatory KYC fields are present before transitioning the status to `COMPLETED` and generating a confirmation (which is the Application ID).


## Testing
Run unit and integration tests with:
```bash
mvn test
```