# VSCode Development Environment for Finance Project

This directory contains a comprehensive VSCode development environment that fully implements the BUILD_AND_RUN_GUIDE.md workflow. The configuration provides automated service management, debugging support, and development utilities for the microservices architecture.

## 🚀 Quick Start

### Option 1: Using VSCode Tasks (Recommended)
1. Open the project in VSCode
2. Press `Ctrl+Shift+P` (or `Cmd+Shift+P` on macOS)
3. Type "Tasks: Run Task" and select it
4. Choose "setup-and-start-all-services"
5. Wait for all services to start automatically

### Option 2: Using Launch Configurations
1. Open the project in VSCode
2. Go to the Run and Debug view (`Ctrl+Shift+D`)
3. Select "Debug All Microservices" or "Run All Microservices"
4. Click the play button

## 📁 Directory Structure

```
.vscode/
├── launch.json          # Debug and run configurations
├── tasks.json           # Automated build and deployment tasks
├── settings.json        # Workspace-specific settings
├── scripts/             # Helper scripts for automation
│   ├── setup-env.js     # Environment setup and validation
│   ├── health-check.js  # Service health monitoring
│   ├── wait-for-service.js # Service dependency management
│   └── cleanup.js       # Workspace cleanup utilities
└── README.md           # This documentation
```

## 🛠 Helper Scripts

### setup-env.js
**Purpose**: Validates and sets up the complete development environment

**Features**:
- Validates Java, Maven, and Docker installations
- Checks environment variables in `.env` file
- Starts Docker infrastructure (PostgreSQL, RabbitMQ)
- Waits for services to be ready
- Builds the common module

**Usage**:
```bash
node .vscode/scripts/setup-env.js
```

### health-check.js
**Purpose**: Comprehensive health monitoring for all services

**Features**:
- Checks infrastructure services (PostgreSQL, RabbitMQ)
- Monitors microservice health endpoints
- Displays detailed component status
- Shows running process information
- Provides summary reports

**Usage**:
```bash
node .vscode/scripts/health-check.js
```

### wait-for-service.js
**Purpose**: Manages service dependencies and startup order

**Features**:
- Waits for specific services to become available
- Handles dependency resolution automatically
- Supports both infrastructure and microservices
- Configurable timeouts
- Proper error handling

**Usage**:
```bash
# Wait for infrastructure
node .vscode/scripts/wait-for-service.js postgres rabbitmq

# Wait for specific microservice
node .vscode/scripts/wait-for-service.js merchant

# Wait for multiple services (dependencies resolved automatically)
node .vscode/scripts/wait-for-service.js ledger payout
```

### cleanup.js
**Purpose**: Comprehensive workspace cleanup and reset

**Features**:
- Stops all Spring Boot processes gracefully
- Stops Docker infrastructure services
- Cleans Maven build artifacts
- Removes temporary files
- Optional deep cleaning of Docker resources

**Usage**:
```bash
# Standard cleanup
node .vscode/scripts/cleanup.js

# Stop only running processes
node .vscode/scripts/cleanup.js --processes-only

# Deep clean including Docker resources
node .vscode/scripts/cleanup.js --deep

# Complete cleanup
node .vscode/scripts/cleanup.js --all
```

## 🎯 VSCode Tasks

### Environment Tasks
- **setup-environment**: Complete environment setup and validation
- **health-check**: Check status of all services
- **cleanup-all**: Stop all services and clean workspace

### Build Tasks
- **build-common-module**: Build the shared common module
- **build-all-services**: Build all microservices
- **build-[service]-service**: Build individual services

### Service Management
- **start-[service]-service-terminal**: Start individual services in separate terminals
- **setup-and-start-all-services**: Complete workflow to start all services
- **restart-all-services**: Stop and restart all services

### Utility Tasks
- **open-service-urls**: Display all service URLs
- **show-running-processes**: Show currently running Spring Boot processes
- **integration-test-workflow**: Complete testing workflow

## 🐛 Launch Configurations

### Debug Configurations
- **Debug Merchant Service**: Debug with port 5005
- **Debug Transaction Service**: Debug with port 5006
- **Debug Ledger Service**: Debug with port 5007
- **Debug Payout Service**: Debug with port 5008

### Run Configurations (Production-like)
- **Run [Service] Service**: Run without debugging
- **Run All Microservices**: Start all services without debugging

### Remote Debug (Attach)
- **Attach to [Service] Service**: Attach debugger to running processes

### Compound Configurations
- **Debug All Microservices**: Start all services in debug mode
- **Debug Core Services**: Start only Merchant + Transaction services
- **Debug Financial Services**: Start only Ledger + Payout services

## 🔧 Service Dependencies

The configuration automatically handles service dependencies:

```
Infrastructure (PostgreSQL, RabbitMQ)
    ↓
Merchant Service (8083)
    ↓
Transaction Service (8082)
    ↓
Ledger Service (8080)
    ↓
Payout Service (8084)
```

## 🌐 Service URLs

Once all services are running:

- **Merchant Service**: http://localhost:8083
  - Swagger UI: http://localhost:8083/swagger-ui.html
  - Health: http://localhost:8083/actuator/health

- **Transaction Service**: http://localhost:8082
  - Swagger UI: http://localhost:8082/swagger-ui.html
  - Health: http://localhost:8082/actuator/health

- **Ledger Service**: http://localhost:8080
  - Swagger UI: http://localhost:8080/swagger-ui.html
  - Health: http://localhost:8080/actuator/health

- **Payout Service**: http://localhost:8084
  - Swagger UI: http://localhost:8084/swagger-ui.html
  - Health: http://localhost:8084/actuator/health

- **RabbitMQ Management**: http://localhost:15672
  - Username: guest
  - Password: guest

## 🔍 Debugging

### Debug Ports
- Merchant Service: 5005
- Transaction Service: 5006
- Ledger Service: 5007
- Payout Service: 5008

### JMX Ports
- Merchant Service: 64792
- Transaction Service: 64794
- Ledger Service: 64791
- Payout Service: 64793

## 📋 Development Workflow

### 1. Initial Setup
```bash
# Run environment setup
node .vscode/scripts/setup-env.js
```

### 2. Start Development
- Use VSCode tasks or launch configurations
- Services start in proper dependency order
- Each service runs in a separate terminal for clear log separation

### 3. Development Cycle
- Make code changes
- Services auto-reload with Spring Boot DevTools
- Use health check to verify service status
- Debug using VSCode debugger

### 4. Testing
- Run integration tests using tasks
- Use health check for service verification
- Test API endpoints via Swagger UI

### 5. Cleanup
```bash
# Clean workspace when done
node .vscode/scripts/cleanup.js
```

## 🚨 Troubleshooting

### Common Issues

**Services won't start**:
1. Run health check: `node .vscode/scripts/health-check.js`
2. Check Docker services are running
3. Verify `.env` file exists and is properly configured
4. Check port availability

**Build failures**:
1. Ensure common module is built first
2. Check Java and Maven versions
3. Clean and rebuild: `mvn clean install`

**Docker issues**:
1. Ensure Docker Desktop is running
2. Check container status: `docker-compose ps`
3. Restart containers: `docker-compose restart`

**Port conflicts**:
1. Check running processes: `node .vscode/scripts/cleanup.js --processes-only`
2. Verify port configuration in `.env` file
3. Use different ports if needed

### Debug Tips

1. **Use separate terminals**: Each service runs in its own terminal for clear log separation
2. **Check health endpoints**: Use `/actuator/health` to verify service status
3. **Monitor dependencies**: Services wait for dependencies before starting
4. **Use JMX monitoring**: Connect JConsole to JMX ports for detailed monitoring

## 🔄 Environment Variables

Required in `.env` file:
```properties
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/finance_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password

# RabbitMQ Configuration
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672

# Service Ports
MERCHANT_SERVER_PORT=8083
TRANSACTION_SERVER_PORT=8082
LEDGER_SERVER_PORT=8080
PAYOUT_SERVER_PORT=8084

# Service URLs (for inter-service communication)
MERCHANT_SERVICE_URL=http://localhost:8083
TRANSACTION_SERVICE_URL=http://localhost:8082
LEDGER_SERVICE_URL=http://localhost:8080
PAYOUT_SERVICE_URL=http://localhost:8084
```

## 📚 Additional Resources

- [BUILD_AND_RUN_GUIDE.md](../BUILD_AND_RUN_GUIDE.md) - Original manual setup guide
- [implementation_plan.md](../implementation_plan.md) - Detailed implementation specifications
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

---

**Note**: This VSCode configuration fully automates the workflow described in BUILD_AND_RUN_GUIDE.md, providing a seamless development experience with proper service orchestration, debugging support, and comprehensive tooling.
