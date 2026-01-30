# NeoCommercePay - Distributed E-Commerce Microservices System

## Architecture Overview

NeoCommercePay is a production-ready, distributed e-commerce platform built using microservices architecture with Spring Boot 3.x, Spring Cloud Gateway, Apache Kafka, PostgreSQL, and MongoDB.

### System Components

```
┌─────────────────────────────────────────────────────────────────┐
│                         API Gateway (8080)                       │
│                   Spring Cloud Gateway + JWT Auth                │
└────────────┬───────────┬───────────┬────────────┬───────────────┘
             │           │           │            │
    ┌────────▼───┐  ┌───▼──────┐  ┌▼──────────┐ ┌▼──────────────┐
    │User Service│  │Product   │  │Order      │ │Payment        │
    │  (8081)    │  │Service   │  │Service    │ │Service        │
    │PostgreSQL  │  │(8082)    │  │(8083)     │ │(8084)         │
    │            │  │MongoDB   │  │PostgreSQL │ │PostgreSQL     │
    └────────────┘  └──────────┘  └───────────┘ └───────────────┘
                            │                              │
                   ┌────────┴────────┬─────────────────────┘
                   │                 │
              ┌────▼──────┐    ┌────▼─────┐
              │  Kafka    │    │Zookeeper │
              │  (9092)   │    │  (2181)  │
              └───────────┘    └──────────┘
```

### Technology Stack

- **Framework**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **API Gateway**: Spring Cloud Gateway
- **Security**: Spring Security + JWT (JJWT 0.12.3)
- **Messaging**: Apache Kafka + Zookeeper
- **Databases**:
  - PostgreSQL 15 (User, Order, Payment services)
  - MongoDB 7.0 (Product service)
- **Build Tool**: Maven 3.9+
- **Java Version**: 17+
- **Containerization**: Docker + Docker Compose
- **API Documentation**: SpringDoc OpenAPI (Swagger)


## Testing the System

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "SecurePass123",
    "fullName": "John Doe"
  }'
```

Response includes JWT token:
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2. Create a Product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "categoryName": "Electronics",
    "stockQuantity": 50
  }'
```

### 3. Create an Order

```bash
curl -X POST "http://localhost:8080/api/orders?userId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -d '[
    {
      "productId": "<PRODUCT_ID>",
      "quantity": 2,
      "price": 999.99
    }
  ]'
```

### 4. Process Payment

After order creation, payment is automatically initiated. To process:

```bash
curl -X POST "http://localhost:8080/api/payments/<PAYMENT_ID>/process" \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

## Event-Driven Architecture

### Kafka Topics

| Topic | Producer | Consumer | Purpose |
|-------|----------|----------|---------|
| `user.created` | User Service | - | User registration event |
| `user.updated` | User Service | - | User profile update event |
| `user.deleted` | User Service | - | User deletion event |
| `product.created` | Product Service | - | Product creation event |
| `product.updated` | Product Service | - | Product update event |
| `product.deleted` | Product Service | - | Product deletion event |
| `inventory.depleted` | Product Service | - | Low inventory alert |
| `order.created` | Order Service | Product Service, Payment Service | New order event |
| `order.updated` | Order Service | - | Order status change event |
| `order.cancelled` | Order Service | - | Order cancellation event |
| `payment.initiated` | Payment Service | - | Payment initialization event |
| `payment.processing` | Payment Service | - | Payment processing event |
| `payment.completed` | Payment Service | Order Service | Successful payment event |
| `payment.failed` | Payment Service | Order Service | Failed payment event |
| `payment.refunded` | Payment Service | - | Payment refund event |

### Retry Mechanism

All Kafka consumers implement exponential backoff retry:
- **Initial delay**: 1 second
- **Multiplier**: 2x
- **Max delay**: 30 seconds
- **Max attempts**: 5

Failed messages are sent to dead-letter queues (DLQ):
- `order-service-dlq`
- `payment-service-dlq`
- `product-service-dlq`

## Security

### JWT Authentication

- JWT tokens expire after 24 hours
- Tokens are validated at the API Gateway
- All endpoints except `/register` and `/login` require authentication

### Password Security

- Passwords are hashed using BCrypt with salt
- Minimum password length: 8 characters

### Database Security

- Each service uses separate PostgreSQL database
- Connection pooling enabled
- Prepared statements prevent SQL injection

## Monitoring & Logging

### Actuator Endpoints

Each service exposes Spring Boot Actuator endpoints:
- `/actuator/health` - Health check
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Correlation IDs

All requests are tagged with correlation IDs for distributed tracing across services and Kafka events.

