# Real-Time Payment Ledger System (WIP)

This project is a microservices-based payment ledger system designed for processing and managing financial transactions in real-time. It is built using Java, Spring Boot, and a suite of other modern technologies to ensure scalability, resilience, and maintainability.

## Architecture Overview

The system follows a microservices architecture, with each service responsible for a specific business domain. The services communicate with each other asynchronously using RabbitMQ, ensuring loose coupling and high availability. PostgreSQL is used as the primary database for data persistence.

The main components of the system are:

-   **Merchant Service:** Manages merchant information.
-   **Transaction Service:** Handles incoming payment transactions.
-   **Ledger Service:** Records all transactions in a ledger.
-   **Payout Service:** Manages payouts to merchants based on their transactions.

The entire environment is orchestrated using Docker Compose, making it easy to set up and run the system locally.

## Technologies Used

-   **Backend:** Java 17, Spring Boot 3.2.0, Spring Cloud 2023.0.0
-   **Database:** PostgreSQL 13
-   **Messaging:** RabbitMQ 3
-   **Build Tool:** Maven
-   **Containerization:** Docker

## Getting Started

Follow these instructions to get the project up and running on your local machine.

### Prerequisites

Make sure you have the following software installed:

-   [Java 17 or higher](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)
-   [Apache Maven](https://maven.apache.org/install.html)
-   [Docker](https://docs.docker.com/get-docker/)
-   [Docker Compose](https://docs.docker.com/compose/install/)

### Configuration

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/your-username/payment-ledger-system.git
    cd payment-ledger-system
    ```

2.  **Set up environment variables:**

    Create a `.env` file in the root directory of the project and add the following content:

    ```
    POSTGRES_USER=postgres
    POSTGRES_PASSWORD=password
    RABBITMQ_DEFAULT_USER=guest
    RABBITMQ_DEFAULT_PASS=guest
    RABBITMQ_ERLANG_COOKIE='secret cookie string'
    ```

### Installation and Running

1.  **Build the project:**

    Build the individual services using Maven:

    ```bash
    mvn clean install -pl merchant-service
    mvn clean install -pl transaction-service
    mvn clean install -pl ledger-service
    mvn clean install -pl payout-service
    ```

    Alternatively, you can build all modules at once from the root directory:

    ```bash
    mvn clean install
    ```

2.  **Start the services:**

    Use Docker Compose to start the entire application stack, including the database and message queue:

    ```bash
    docker-compose up --build
    ```

    The services will be available at the following ports:

-   **Merchant Service:** `http://localhost:8083`
-   **Transaction Service:** `http://localhost:8082`
-   **Ledger Service:** `http://localhost:8080`
-   **Payout Service:** `http://localhost:8084`
    -   **RabbitMQ Management:** `http://localhost:15672`
    -   **PostgreSQL:** `localhost:5432`

## Services

### Merchant Service

-   **Description:** Manages merchant data, including creation, retrieval, and updates.
-   **Port:** `8083`

#### API Endpoints

-   `POST /api/merchants`: Creates a new merchant.
-   `GET /api/merchants/{id}`: Retrieves a merchant by their ID.

### Transaction Service

-   **Description:** Receives and processes incoming payment transactions. It validates transactions and publishes them to the message queue for further processing.
-   **Port:** `8082`

#### API Endpoints

-   `POST /api/transactions`: Submits a new transaction for processing.

### Ledger Service

-   **Description:** Subscribes to transaction events and records them in the ledger. It maintains a detailed history of all financial activities.
-   **Port:** `8080`

#### API Endpoints

-   `GET /api/ledgers/payable`: Retrieves all ledgers that are ready for payout.

### Payout Service

-   **Description:** Calculates and processes payouts for merchants based on their settled transactions. It communicates with other services to get the necessary data for payout calculations.
-   **Port:** `8084`

#### API Endpoints

-   `GET /api/payouts/status/{id}`: Retrieves the status of a specific payout by its ID.

## Database

The project uses a PostgreSQL database to store data for all services. The initial database schema and some sample data are created by the `postgres-init/init.sql` script when the database container starts for the first time.

## Message Queue

RabbitMQ is used for asynchronous communication between the microservices. This decouples the services and allows for better scalability and fault tolerance. The RabbitMQ management interface is available at `http://localhost:15672` for monitoring queues and messages.
