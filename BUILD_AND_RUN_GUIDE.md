# Finance Project - Build and Run Guide

This guide provides step-by-step instructions for building and running all microservices in the correct order.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- Git

## Project Structure

```
finance-project/
├── common/                 # Shared utilities and enums
├── merchant-service/       # Merchant management service
├── transaction-service/    # Transaction processing service
├── ledger-service/        # Ledger and accounting service
├── payout-service/        # Payout processing service
├── docker-compose.yml     # Infrastructure services
├── .env                   # Environment variables
└── pom.xml               # Parent Maven configuration
```

## Step 1: Start Infrastructure Services

First, start the required infrastructure services (PostgreSQL and RabbitMQ):

```bash
docker-compose up -d
```

This will start:
- **PostgreSQL** on port 5432
- **RabbitMQ** on port 5672 (AMQP) and 15672 (Management UI)

Verify the services are running:
```bash
docker ps
```

## Step 2: Environment Configuration

The project uses environment variables defined in `.env` file. Ensure the following variables are set:

```bash
# Database Configuration
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/transactions_db
SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}

# RabbitMQ Configuration
RABBITMQ_DEFAULT_USER=guest
RABBITMQ_DEFAULT_PASS=guest
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672

# Service URLs
MERCHANT_SERVICE_URL=http://localhost:8083
TRANSACTION_SERVICE_URL=http://localhost:8082
LEDGER_SERVICE_URL=http://localhost:8080
PAYOUT_SERVICE_URL=http://localhost:8084

# Server Ports
MERCHANT_SERVER_PORT=8083
TRANSACTION_SERVER_PORT=8082
LEDGER_SERVER_PORT=8080
PAYOUT_SERVER_PORT=8084
```

## Step 3: Build Services in Order

### 3.1 Build Common Module (Required First)

The common module contains shared utilities and enums that other services depend on:

```bash
mvn clean install -pl common
```

### 3.2 Build All Services

Build all services in the correct order:

```bash
mvn clean compile -pl merchant-service && \
mvn clean compile -pl transaction-service && \
mvn clean compile -pl ledger-service && \
mvn clean compile -pl payout-service
```

## Step 4: Run Services in Correct Order

**IMPORTANT**: Services must be started in this specific order: **common → merchant → transaction → ledger → payout**

### Option A: Run Services in Separate Terminals (Recommended)

Open **4 separate terminal windows/tabs** and run each service in its own terminal:

**Terminal 1 - Merchant Service:**
```bash
export $(cat .env | xargs)
SERVER_PORT=$MERCHANT_SERVER_PORT mvn spring-boot:run -pl merchant-service
```

**Terminal 2 - Transaction Service (start after merchant is up):**
```bash
export $(cat .env | xargs)
SERVER_PORT=$TRANSACTION_SERVER_PORT mvn spring-boot:run -pl transaction-service
```

**Terminal 3 - Ledger Service (start after transaction is up):**
```bash
export $(cat .env | xargs)
SERVER_PORT=$LEDGER_SERVER_PORT mvn spring-boot:run -pl ledger-service
```

**Terminal 4 - Payout Service (start after ledger is up):**
```bash
export $(cat .env | xargs)
SERVER_PORT=$PAYOUT_SERVER_PORT mvn spring-boot:run -pl payout-service
```

### Option B: Run Services in Background (All at Once)

If you prefer to run all services in background processes from a single terminal:

```bash
export $(cat .env | xargs) && \
SERVER_PORT=$MERCHANT_SERVER_PORT mvn spring-boot:run -pl merchant-service & \
SERVER_PORT=$TRANSACTION_SERVER_PORT mvn spring-boot:run -pl transaction-service & \
SERVER_PORT=$LEDGER_SERVER_PORT mvn spring-boot:run -pl ledger-service & \
SERVER_PORT=$PAYOUT_SERVER_PORT mvn spring-boot:run -pl payout-service &
```

### Option C: Run Services with Delays (Sequential Background)

Run services in background with delays to ensure proper startup order:

```bash
export $(cat .env | xargs) && \
SERVER_PORT=$MERCHANT_SERVER_PORT mvn spring-boot:run -pl merchant-service & \
sleep 30 && \
SERVER_PORT=$TRANSACTION_SERVER_PORT mvn spring-boot:run -pl transaction-service & \
sleep 30 && \
SERVER_PORT=$LEDGER_SERVER_PORT mvn spring-boot:run -pl ledger-service & \
sleep 30 && \
SERVER_PORT=$PAYOUT_SERVER_PORT mvn spring-boot:run -pl payout-service &
```

### Benefits of Running in Separate Terminals

- **Individual Log Monitoring**: Each service's logs are visible in its own terminal
- **Easy Debugging**: Easier to identify which service has issues
- **Independent Control**: Can stop/restart individual services without affecting others
- **Better Development Experience**: Clear separation of concerns during development

## Step 5: Verify Services are Running

### Check Health Endpoints

Wait about 30 seconds for services to start, then check health endpoints:

```bash
# Merchant Service
curl http://localhost:8083/actuator/health

# Transaction Service
curl http://localhost:8082/actuator/health

# Ledger Service
curl http://localhost:8080/actuator/health

# Payout Service
curl http://localhost:8084/actuator/health
```

All should return `{"status":"UP"}` or similar.

### Check Running Processes

```bash
ps aux | grep "spring-boot:run" | grep -v grep
```

You should see 4 Maven processes running.

### Check Service Ports

```bash
netstat -an | grep LISTEN | grep -E "(8080|8082|8083|8084)"
```

## Service Information

| Service | Port | Health Check | Swagger UI |
|---------|------|--------------|------------|
| Merchant Service | 8083 | `/actuator/health` | `/swagger-ui.html` |
| Transaction Service | 8082 | `/actuator/health` | `/swagger-ui.html` |
| Ledger Service | 8080 | `/actuator/health` | `/swagger-ui.html` |
| Payout Service | 8084 | `/actuator/health` | `/swagger-ui.html` |

## Troubleshooting

### Common Issues and Solutions

#### 1. Database Connection Issues
**Error**: `Failed to determine suitable jdbc url`

**Solution**: Ensure PostgreSQL is running and environment variables are exported:
```bash
docker-compose up -d postgres
export $(cat .env | xargs)
```

#### 2. Port Already in Use
**Error**: `Port 8080 was already in use`

**Solution**: Kill existing processes or change ports in `.env` file:
```bash
# Kill existing processes
pkill -f "spring-boot:run"

# Or change ports in .env file
```

#### 3. RabbitMQ Connection Issues
**Error**: `Connection refused to localhost:5672`

**Solution**: Ensure RabbitMQ is running:
```bash
docker-compose up -d rabbitmq
```

#### 4. Import Issues in Payout Service
**Error**: `cannot find symbol: class PayoutStatus`

**Solution**: This has been fixed. PayoutStatus is now imported from common module.

#### 5. Terminal Management Issues
**Issue**: Need to manage multiple terminals efficiently

**Solutions**:
- Use terminal multiplexers like `tmux` or `screen` for better terminal management
- Use IDE integrated terminals (VS Code, IntelliJ) with multiple terminal tabs
- Name your terminal tabs/windows for easy identification (e.g., "Merchant", "Transaction", etc.)

### Stopping Services

**If running in separate terminals:**
- Press `Ctrl+C` in each terminal window to stop individual services
- Or close the terminal windows

**If running in background:**
```bash
# Stop Spring Boot applications
pkill -f "spring-boot:run"

# Stop infrastructure services
docker-compose down
```

### Logs

**For separate terminals**: Logs are directly visible in each service's terminal window

**For background processes**: Use these commands to view logs:
```bash
# View all Java processes
jps -v | grep spring-boot

# View logs using process ID
tail -f /proc/[PID]/fd/1
```

## Development Workflow

### For Separate Terminal Setup:
1. **Make changes** to any service
2. **Stop the specific service**: Press `Ctrl+C` in the service's terminal
3. **Rebuild**: `mvn clean compile -pl service-name`
4. **Restart**: Re-run the service command in the same terminal

### For Background Process Setup:
1. **Make changes** to any service
2. **Stop the specific service**: `pkill -f "service-name"`
3. **Rebuild**: `mvn clean compile -pl service-name`
4. **Restart**: `SERVER_PORT=$SERVICE_PORT mvn spring-boot:run -pl service-name &`

## API Testing

Once all services are running, you can:

1. **Access Swagger UI** for each service at `http://localhost:PORT/swagger-ui.html`
2. **Test APIs** using curl, Postman, or any HTTP client
3. **Monitor RabbitMQ** at `http://localhost:15672` (guest/guest)

## Build and Run Order Summary

**Critical**: Always follow this exact sequence:

### 1. Infrastructure Setup
```bash
docker-compose up -d
```

### 2. Build Phase (Sequential)
```bash
# Step 1: Build common module (REQUIRED FIRST)
mvn clean install -pl common

# Step 2: Build all services
mvn clean compile -pl merchant-service && \
mvn clean compile -pl transaction-service && \
mvn clean compile -pl ledger-service && \
mvn clean compile -pl payout-service
```

### 3. Run Phase (Sequential Order)
**Order**: common → merchant → transaction → ledger → payout

**Recommended**: Use separate terminals for each service
- Terminal 1: Merchant Service (port 8083)
- Terminal 2: Transaction Service (port 8082) 
- Terminal 3: Ledger Service (port 8080)
- Terminal 4: Payout Service (port 8084)

### 4. Verification
```bash
# Check all services are healthy
curl http://localhost:8083/actuator/health  # Merchant
curl http://localhost:8082/actuator/health  # Transaction  
curl http://localhost:8080/actuator/health  # Ledger
curl http://localhost:8084/actuator/health  # Payout
```

**Key Points**:
- The common module must be built first as it contains shared dependencies
- Services should be started in the specified order for proper dependency resolution
- Using separate terminals provides better log visibility and service management
- Wait for each service to fully start before starting the next one
