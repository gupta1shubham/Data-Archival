Data Archival System
This repository outlines the system design for a Data Archival Service that moves data from a primary source database to an archival database using a microservices architecture. The system supports event-driven communication, asynchronous processing, and RBAC (Role-Based Access Control) to ensure performance, security, and scalability.

1. High-Level Architecture
The system design includes 6 key microservices and supporting components like the source database, archival database, and observability stack. The architecture follows an event-driven approach to enable asynchronous data processing for scalability, performance, and resilience.

Microservices
Configuration Service (Metadata Service)
Scheduler Service
Archival Service
Deletion Service
RBAC & Security Service
Viewer Service (Query Service for Archived Data)
Supporting Components
Source Database: The main application database (e.g., PostgreSQL, MySQL).
Archival Database: The secondary database where archived data resides (cheaper storage options like PostgreSQL, S3, etc.).
Message Broker: Kafka or RabbitMQ for event-driven communication.
Monitoring & Logging: Prometheus, Grafana, ELK stack for monitoring and logging.
Distributed Tracing: OpenTelemetry for tracing distributed microservices.
2. Microservices Design
1. Configuration Service (Metadata Service)
Responsibilities:

Stores table-specific rules for archival and deletion.
Provides a self-service API for administrators to manage archival rules.
Database:

Stores metadata for each table.
APIs:

POST /api/configure — Configure table-specific archival rules.
GET /api/configure/{tableName} — Get the rules for a specific table.
Data Schema (archival_criteria)

sql
Copy code
CREATE TABLE archival_criteria (
    table_name VARCHAR(255) PRIMARY KEY,
    archive_after_days INT,
    delete_after_days INT
);
2. Scheduler Service
Responsibilities:

Triggers archival and deletion jobs at scheduled intervals.
Queries the Configuration Service to get active archival rules.
Triggering Mechanism:

Uses Quartz Scheduler or AWS EventBridge to schedule periodic tasks.
APIs (Internal, not Public):

POST /api/schedule/archive — Triggers archival job for a specific table.
POST /api/schedule/delete — Triggers deletion job for a specific table.
Workflow:

Scheduler queries Configuration Service for tables eligible for archival or deletion.
Scheduler publishes tasks to Kafka for Archival Service and Deletion Service.
3. Archival Service
Responsibilities:

Moves data from the source database to the archival database.
Listens to "archive tasks" from Kafka and processes them asynchronously.
Key Features:

Processes data in batches to avoid overwhelming the source database.
Uses idempotency tokens to ensure retries are safe.
APIs (Internal Only):

POST /api/archive/{tableName} — Trigger archival for a specific table.
Communication:

Listens to Kafka topics for archival jobs.
Workflow:

Listens for new "archive task" events from Kafka.
Queries Source DB for eligible data.
Copies the data to the archival database in bulk (batch processing).
Updates job status (completed/failed) in Job Tracking Table.
Database Table for Job Tracking

sql
Copy code
CREATE TABLE archive_jobs (
    job_id UUID PRIMARY KEY,
    table_name VARCHAR(255),
    status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
4. Deletion Service
Responsibilities:

Deletes old data from the source and archival databases.
Triggering Mechanism:

Listens to "deletion tasks" from Kafka or is triggered by Scheduler Service.
APIs (Internal Only):

POST /api/delete/{tableName} — Trigger deletion for a specific table.
Key Features:

Processes deletions in batches to avoid large locks on the source DB.
Workflow:

Listens for "deletion task" events from Kafka.
Deletes data from source database and archival database.
Tracks completion status in the Job Tracking Table.
5. RBAC & Security Service
Responsibilities:

Role-based access control (RBAC) for accessing archived data.
Issues JWT tokens for service-to-service and user authentication.
APIs:

POST /api/auth/login — Authenticate user and issue JWT token.
POST /api/auth/roles — Manage user roles.
JWT Claims:

Embeds user roles in JWT claims for easy validation.
6. Viewer Service (Query Archived Data)
Responsibilities:

Provides a REST API to view archived data.
Enforces RBAC permissions using JWT tokens.
Only allows access to archived data the user is authorized to view.
APIs:

GET /api/view/{tableName} — View archived data from the archival database.
Features:

Uses pagination and filtering to manage large datasets.
Applies RBAC rules (only admin or authorized users can view specific tables).
3. Data Flow (Sequence Diagram)
Admin Configures Rules
Admin → Configuration Service → Save rules in archival_criteria table.
Scheduler Triggers Archive Job
Scheduler Service → Kafka → Archival Service.
Archival Service Processes Data
Archival Service queries Source DB, transfers data to Archival DB.
Viewer Queries Archived Data
User → Viewer Service → RBAC Validation → Query Archival DB.
4. Design Decisions
Data Movement
Batch processing for moving large datasets.
ETL strategy for large datasets, with batch size of 1000–5000 rows.
Security
RBAC for Table Access — Access control for different users.
JWT for Authentication — Stateless token-based authentication.
Data Deletion
Ensure safe, batch-by-batch deletion.
Idempotent Jobs — Use unique tokens for tracking archive and delete jobs.
Scalability
Decouple Scheduler, Archival, Deletion via Kafka event-driven system.
Technologies Used
Spring Boot — For microservices development.
PostgreSQL — As the source and archival databases.
Kafka — For event-driven communication.
Prometheus + Grafana — For monitoring and visualization.
JWT — For user authentication and service-to-service authorization.
ELK Stack — For centralized logging.
Quartz Scheduler — For scheduling archive and deletion jobs.
Setup Instructions
Clone the Repository

bash
Copy code
git clone https://github.com/your-repo/data-archival-system.git
Start Services Using Docker Compose

bash
Copy code
docker-compose up -d
Access Services

Viewer Service: http://localhost:8085
RBAC Service: http://localhost:8086
Prometheus: http://localhost:9090
Grafana: http://localhost:3000
Test APIs Using Postman

Login API: /api/auth/login
View Archived Data: /api/view/{tableName}