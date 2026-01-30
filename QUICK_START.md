# NeoCommercePay Quick Start Guide

## Prerequisites Check

Run these commands to verify your environment:

```bash
# Check Java version (must be 17+)
java -version

# Check Maven version (must be 3.8+)
mvn -version

# Check Docker version
docker --version

# Check Docker Compose version
docker-compose --version
```

## 5-Minute Setup

### Step 1: Build

```bash
mvn clean install
```

Expected output: `BUILD SUCCESS` for all modules

### Step 2: Start Services

```bash
docker-compose up -d
```

### Step 3: Verify

Wait 2-3 minutes for all services to start, then:

```bash
docker-compose ps
```

All services should show `healthy` or `running`.

### Step 4: Test

```bash
# Register a user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test1234","fullName":"Test User"}'
```

You should receive a response with a JWT token!

## Service URLs

| Service | URL | Swagger UI |
|---------|-----|------------|
| API Gateway | http://localhost:8080 | - |
| User Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html |
| Product Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html |
| Order Service | http://localhost:8083 | http://localhost:8083/swagger-ui.html |
| Payment Service | http://localhost:8084 | http://localhost:8084/swagger-ui.html |

## Complete Test Scenario

### 1. Register & Login

```bash
# Register
RESPONSE=$(curl -s -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"SecurePass123","fullName":"John Doe"}')

# Extract token
TOKEN=$(echo $RESPONSE | jq -r '.token')
echo "Token: $TOKEN"
```

### 2. Create a Product

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop with RTX 4080",
    "price": 1999.99,
    "categoryName": "Electronics",
    "stockQuantity": 100
  }'
```

Save the product `id` from the response.

### 3. Create an Order

```bash
curl -X POST "http://localhost:8080/api/orders?userId=1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '[
    {
      "productId": "PRODUCT_ID_HERE",
      "quantity": 1,
      "price": 1999.99
    }
  ]'
```

This automatically:
- Decrements product inventory
- Initiates payment
- Creates payment record

### 4. Get Payment Status

```bash
# Get order to find payment
curl -X GET "http://localhost:8080/api/orders/1" \
  -H "Authorization: Bearer $TOKEN"

# Get payment status
curl -X GET "http://localhost:8080/api/payments/1/status" \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Process Payment

```bash
curl -X POST "http://localhost:8080/api/payments/1/process" \
  -H "Authorization: Bearer $TOKEN"
```

The mock payment processor has 90% success rate.

## Common Commands

### View Logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f user-service
docker-compose logs -f product-service
docker-compose logs -f order-service
docker-compose logs -f payment-service
```

### Restart a Service

```bash
docker-compose restart user-service
```

### Stop All Services

```bash
docker-compose down
```

### Stop and Remove Volumes (Clean Start)

```bash
docker-compose down -v
```

### Rebuild After Code Changes

```bash
mvn clean install
docker-compose up -d --build
```

## Troubleshooting

### Services Not Starting

```bash
# Check logs
docker-compose logs

# Verify ports not in use
netstat -an | grep 8080
netstat -an | grep 5432
netstat -an | grep 27017
netstat -an | grep 9092
```

### Database Issues

```bash
# Access PostgreSQL
docker exec -it neocommercepay-postgres psql -U postgres

# List databases
\l

# Connect to database
\c neocommercepay_users

# List tables
\dt
```

```bash
# Access MongoDB
docker exec -it neocommercepay-mongodb mongosh

# Show databases
show dbs

# Use database
use neocommercepay_products

# Show collections
show collections
```

### Kafka Issues

```bash
# List topics
docker exec -it neocommercepay-kafka kafka-topics \
  --list \
  --bootstrap-server localhost:9092

# Check consumer groups
docker exec -it neocommercepay-kafka kafka-consumer-groups \
  --list \
  --bootstrap-server localhost:9092

# Describe consumer group
docker exec -it neocommercepay-kafka kafka-consumer-groups \
  --describe \
  --group order-service \
  --bootstrap-server localhost:9092
```

### Reset Everything

```bash
# Stop all services
docker-compose down

# Remove all volumes
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Rebuild
mvn clean install
docker-compose up -d --build
```

## Development Workflow

### Make Changes to Code

1. Stop the service:
```bash
docker-compose stop user-service
```

2. Make your code changes

3. Rebuild:
```bash
mvn clean install -pl user-service -am
```

4. Restart:
```bash
docker-compose up -d --build user-service
```

### Run Tests

```bash
# Run all tests
mvn test

# Run tests for specific service
mvn test -pl user-service
```

### Access Swagger UI

Each service has interactive API documentation:
- http://localhost:8081/swagger-ui.html (User Service)
- http://localhost:8082/swagger-ui.html (Product Service)
- http://localhost:8083/swagger-ui.html (Order Service)
- http://localhost:8084/swagger-ui.html (Payment Service)

## Monitoring

### Health Checks

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# All services
for port in 8081 8082 8083 8084; do
  echo "Port $port:"
  curl -s http://localhost:$port/actuator/health | jq
done
```

### Metrics

```bash
# User Service metrics
curl http://localhost:8081/actuator/metrics

# Specific metric
curl http://localhost:8081/actuator/metrics/jvm.memory.used
```

## Performance Testing

### Load Test with Apache Bench

```bash
# Register 100 users
ab -n 100 -c 10 -T "application/json" \
  -p register.json \
  http://localhost:8080/api/users/register
```

Where `register.json` contains:
```json
{"email":"load.test@example.com","password":"Test1234","fullName":"Load Test"}
```

## Next Steps

1. Explore API documentation in Swagger UI
2. Review logs to understand event flow
3. Monitor Kafka topics
4. Test error scenarios (invalid data, failed payments)
5. Scale services and test load balancing
6. Review ARCHITECTURE.md for detailed design

## Getting Help

- Check logs: `docker-compose logs [service-name]`
- View README-JAVA.md for comprehensive documentation
- View ARCHITECTURE.md for architecture details
- Check GitHub issues for known problems

## Quick Reference

### Default Credentials

- PostgreSQL: `postgres` / `postgres`
- MongoDB: No authentication required
- JWT Secret: Set in `.env` file

### Port Mapping

- 8080: API Gateway
- 8081: User Service
- 8082: Product Service
- 8083: Order Service
- 8084: Payment Service
- 5432: PostgreSQL
- 27017: MongoDB
- 9092: Kafka
- 2181: Zookeeper

### Important Endpoints

- Register: `POST /api/users/register`
- Login: `POST /api/users/login`
- Create Product: `POST /api/products`
- Create Order: `POST /api/orders`
- Process Payment: `POST /api/payments/{id}/process`

---

**You're all set! Start building with NeoCommercePay!**
