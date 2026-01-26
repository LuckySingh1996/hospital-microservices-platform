# 🏥 Billing & Payment Service

Production-grade microservice for hospital billing and payment processing with Kafka event-driven architecture.

## 📋 Features

✅ **Automatic Bill Generation** - Listen to appointment.booked events  
✅ **Partial Payments Support** - Pay bills in multiple installments  
✅ **Idempotent Payments** - Prevent double payments using idempotency keys  
✅ **Transaction Safety** - SERIALIZABLE isolation for payment processing  
✅ **Event-Driven** - Publishes bill.generated, payment.completed, payment.failed events  
✅ **Custom Metrics** - payments_success_total, payments_failure_total  

## 🚀 Quick Start

```bash
# Build
mvn clean install

# Run with Docker
docker-compose up -d

# Health Check
curl http://localhost:8083/actuator/health
```

## 📡 API Endpoints

### Bills

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/bills` | Create bill manually |
| GET | `/api/v1/bills/{billId}` | Get bill by ID |
| GET | `/api/v1/bills/number/{billNumber}` | Get bill by number |

### Payments

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/payments` | Process payment |
| GET | `/api/v1/payments/{paymentReference}` | Get payment details |

## 📥 Request Examples

### Create Bill

```json
POST /api/v1/bills
Authorization: Bearer <JWT_TOKEN>

{
  "appointmentId": 1,
  "patientId": 1,
  "patientName": "John Doe",
  "consultationFee": 500.00,
  "labCharges": 200.00,
  "pharmacyCharges": 150.00
}
```

### Process Payment (Idempotent)

```json
POST /api/v1/payments
Authorization: Bearer <JWT_TOKEN>

{
  "billId": 1,
  "amount": 500.00,
  "paymentMethod": "CARD",
  "idempotencyKey": "payment-123-unique-key"
}
```

**Note:** Same `idempotencyKey` will return same result without duplicate processing.

## 🔄 Kafka Event Flow

### Consumes

- **appointment.booked** → Automatically generates bill

### Produces

- **bill.generated** → Notifies billing completion
- **payment.completed** → Notifies successful payment
- **payment.failed** → Notifies payment failure

## 🛡️ Failure Handling

### Duplicate Billing Prevention

```java
if (billRepository.existsByAppointmentId(appointmentId)) {
    throw new DuplicateBillException("Bill already exists");
}
```

### Duplicate Payment Prevention (Idempotency)

```java
var existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
if (existing.isPresent()) {
    throw new DuplicatePaymentException("Already processed");
}
```

### Transaction Safety

```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public PaymentResponse processPayment(CreatePaymentRequest request) {
    // Thread-safe payment processing
}
```

## 📊 Database Schema

### Bills Table

```sql
CREATE TABLE bills (
    id BIGSERIAL PRIMARY KEY,
    bill_number VARCHAR(50) UNIQUE NOT NULL,
    appointment_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL,
    lab_charges DECIMAL(10,2),
    pharmacy_charges DECIMAL(10,2),
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) NOT NULL,
    due_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT
);
```

### Payments Table

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_reference VARCHAR(50) UNIQUE NOT NULL,
    bill_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    idempotency_key VARCHAR(100) UNIQUE NOT NULL,
    transaction_id VARCHAR(50),
    created_at TIMESTAMP NOT NULL
);
```

## 🔐 Security

All endpoints require JWT authentication with roles:
- **ADMIN** - Full access
- **RECEPTIONIST** - Create bills, process payments
- **DOCTOR** - View bills and payments

## 📈 Metrics

Access metrics at: `http://localhost:8083/actuator/prometheus`

Custom metrics:
- `payments_success_total` - Total successful payments
- `payments_failure_total` - Total failed payments

## 🧪 Testing

### 1. Automatic Bill Generation

```bash
# Create appointment in Appointment Service
# Bill is automatically generated via Kafka

# Check Kafka events
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic bill.generated \
  --from-beginning
```

### 2. Manual Bill Creation

```bash
curl -X POST http://localhost:8083/api/v1/bills \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "appointmentId": 1,
    "patientId": 1,
    "patientName": "John Doe",
    "consultationFee": 500.00
  }'
```

### 3. Process Payment

```bash
curl -X POST http://localhost:8083/api/v1/payments \
  -H 'Authorization: Bearer YOUR_TOKEN' \
  -H 'Content-Type: application/json' \
  -d '{
    "billId": 1,
    "amount": 300.00,
    "paymentMethod": "CARD",
    "idempotencyKey": "payment-001"
  }'
```

### 4. Test Idempotency (Duplicate Payment Prevention)

```bash
# Run same payment request twice
# Second request returns same result without duplicate processing
```

### 5. Partial Payment

```bash
# Payment 1: Pay 300 of 850 total
curl -X POST http://localhost:8083/api/v1/payments \
  -d '{"billId": 1, "amount": 300.00, "paymentMethod": "CASH", "idempotencyKey": "pay-1"}'

# Check bill status: PARTIALLY_PAID, due: 550

# Payment 2: Pay remaining 550
curl -X POST http://localhost:8083/api/v1/payments \
  -d '{"billId": 1, "amount": 550.00, "paymentMethod": "CARD", "idempotencyKey": "pay-2"}'

# Check bill status: PAID, due: 0
```

## 🏗️ Architecture Highlights

### Event-Driven Design

```
Appointment Service → appointment.booked
                          ↓
                   Billing Service
                          ↓
                   bill.generated
                          ↓
                   (Payment Processing)
                          ↓
            payment.completed | payment.failed
```

### Idempotency Pattern

```java
// Client generates unique key
idempotencyKey = "payment-" + UUID.randomUUID()

// Server checks for duplicate
if (existsByIdempotencyKey(key)) {
    return cached result // No re-processing
}

// Process payment
save payment with idempotency key
```

## 🚨 Error Handling

| Error | Status | Description |
|-------|--------|-------------|
| DuplicateBillException | 409 | Bill already exists for appointment |
| DuplicatePaymentException | 409 | Payment already processed (idempotency) |
| PaymentProcessingException | 400 | Payment gateway failure |
| ResourceNotFoundException | 404 | Bill or payment not found |

## ⚙️ Configuration

Environment variables:

```yaml
DB_HOST: billing-db
DB_PORT: 5432
DB_NAME: billing_db
KAFKA_BOOTSTRAP_SERVERS: kafka:9092
JWT_SECRET: your-secret-key
```

## 📦 Dependencies

- Spring Boot 4.0.2
- Spring Data JPA
- Spring Kafka
- PostgreSQL
- JWT (jjwt 0.12.3)
- Micrometer + Prometheus

## 🔍 Troubleshooting

**Issue: Duplicate bill created**
```bash
# Check logs
docker logs billing-service

# Verify unique constraint
SELECT appointment_id, COUNT(*) FROM bills GROUP BY appointment_id HAVING COUNT(*) > 1;
```

**Issue: Payment processed twice**
```bash
# Check idempotency keys
SELECT idempotency_key, COUNT(*) FROM payments GROUP BY idempotency_key HAVING COUNT(*) > 1;
```

## 📝 Notes

- Bills are automatically generated when appointments are booked
- Partial payments are supported - bill status updates automatically
- Idempotency keys must be unique per payment attempt
- Failed Kafka messages are automatically retried
- Payment gateway simulation adds 100ms delay

## 🎯 Assignment Compliance

✅ POST /bills endpoint  
✅ POST /payments endpoint  
✅ GET /bills/{billId} endpoint  
✅ Partial payments support  
✅ Idempotent payment processing  
✅ Transaction-safe operations (SERIALIZABLE)  
✅ Kafka event publishing/consuming  
✅ Duplicate billing prevention  
✅ Double payment prevention  
✅ Custom metrics (payments_success_total, payments_failure_total)  
✅ JWT authentication  
✅ Role-based access control  
✅ Docker & docker-compose ready  

---

**Service Port:** 8083  
**Database Port:** 5434  
**Spring Boot Version:** 4.0.2  
**Java Version:** 21