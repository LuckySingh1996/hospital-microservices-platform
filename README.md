# Hospital Billing & Appointment Platform

## Overview

This project implements a fault-tolerant, event-driven hospital backend platform using a microservices architecture. The system manages patients, appointments, billing, and payments, and demonstrates scalability, data consistency, asynchronous processing, and real-world hospital workflows.

This solution was developed as a backend system design assignment with a focus on correctness, resilience, and clean architecture.

## Microservices Architecture

### Implemented Services

| Service | Description |
|------|------------|
| Patient Service | Manages patient registration and demographic details |
| Appointment Service | Books doctor appointments and publishes events |
| Billing & Payment Service | Generates bills, processes payments, ensures idempotency |
| Notification Service | Event-driven (documented, intentionally minimal) |


## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA (Hibernate)
- Apache Kafka
- PostgreSQL
- Docker & Docker Compose
- Maven
- Git

## Event Flow

1. Appointment booking triggers an `appointment.booked` Kafka event
2. Billing service consumes the event and generates a bill
3. Payment processing publishes success or failure events

## Idempotency Strategy

- Payments use idempotency keys enforced via database constraints
- Duplicate payment requests return existing transactions
- One bill per appointment enforced at database level

## Failure Handling

- Kafka-based asynchronous communication
- Transactional database operations
- Retry via Kafka consumer mechanisms
- Duplicate billing and payments prevented

## Observability

- Spring Boot Actuator enabled
- Custom Micrometer metrics:
  - appointments_booked_total
  - payments_success_total
  - payments_failure_total
- Metrics available via /actuator/metrics

## Running the System

```bash
docker compose build
docker compose up
```

### Service Ports

| Service | Port |
|------|-----|
| Patient Service | 8081 |
| Appointment Service | 8082 |
| Billing Service | 8083 |


## Database Strategy

Hibernate auto-DDL is used to create tables on startup.

## Assumptions & Trade-offs

- Notification service kept minimal and documented
- No UI or external messaging integrations
- Focus on backend correctness and event-driven design

## Repository Structure

```
root/
├── docker-compose.yml
├── README.md
├── patient-service/
├── appointment-service/
├── billing-service/
```

---

## Status

Submission-ready, dockerized, and interview-aligned.
