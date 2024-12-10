**Data Archival System**
========================

The **Data Archival System** is a **microservices-based, event-driven system** for efficiently **archiving and managing large datasets** from a **primary source database** to a **secondary archival database**. The system ensures scalability, performance, and security with **RBAC (Role-Based Access Control)** and **JWT-based authentication**. It leverages **Kafka** for asynchronous event-driven processing, **PostgreSQL** for data storage, and **Spring Boot** for microservice development.

* * * * *

**High-Level Architecture**
---------------------------

The system consists of **6 key microservices** and supporting components like the **source database**, **archival database**, and **monitoring stack**. The architecture follows an **event-driven approach** to ensure **asynchronous processing, scalability, and fault tolerance**.

### **Microservices**

-   **Configuration Service (Metadata Service)**
-   **Scheduler Service**
-   **Archival Service**
-   **Deletion Service**
-   **RBAC & Security Service**
-   **Viewer Service (Query Service for Archived Data)**

### **Supporting Components**

-   **Source Database**: The main application database (**PostgreSQL**).
-   **Archival Database**: Secondary storage for archived data (**PostgreSQL**).
-   **Message Broker**: Handles communication between services (**Kafka**).

* * * * *

**Microservices Design**
------------------------

* * * * *

### **1\. Configuration Service (Metadata Service)**

**Responsibilities:**

-   Stores **table-specific rules** for archival and deletion.
-   Provides a self-service **API** for managing archival rules.

**API Endpoints**

-   **POST /api/configs** --- Configure archival rules for a table.
-   **GET /api/configs/enabled** --- Get the rules for a specific table.


### **2\. Scheduler Service**

**Responsibilities:**

-   Triggers **archival and deletion jobs** at scheduled intervals.
-   Queries the **Configuration Service** to get active archival rules.

**API Endpoints**

-   **POST /api/schedule/archive** --- Triggers an archival job.
-   **POST /api/schedule/delete** --- Triggers a deletion job.

**Flow**

1.  **Scheduler requests rules** from **Configuration Service**.
2.  **Scheduler publishes tasks** to **Kafka** for Archival and Deletion services.

* * * * *

### **3\. Archival Service**

**Responsibilities:**

-   Moves data from the source database to the archival database.
-   Listens for **archive tasks** from Kafka and processes them asynchronously.

**Key Features:**

-   Processes data in **batches** to avoid overwhelming the source database.
-   Uses **idempotency tokens** to ensure retries are safe.

**API Endpoints (Internal Only)**

-   **POST /api/archive/{tableName}** --- Trigger archival for a specific table.

**Workflow**

1.  Listens for new **"archive task"** events from Kafka.
2.  Queries the **Source Database** for eligible data.
3.  **Transfers the data** to the **Archival Database** in bulk (batch processing).
4.  Updates the **job status** (completed/failed) in the **Job Tracking Table**.


### **4\. Deletion Service**

**Responsibilities:**

-   Deletes old data from the source and archival databases.


**Key Features:**

-   Processes deletions **in batches** to avoid large locks on the source DB.

**Workflow**

1.  Listens for **"deletion task"** events from Kafka.
2.  Deletes data from **Source Database** and **Archival Database**.
3.  Tracks completion status in the **Job Tracking Table**.

* * * * *

### **5\. RBAC & Security Service**

**Responsibilities:**

-   Implements **Role-Based Access Control (RBAC)** for accessing archived data.
-   Issues **JWT tokens** for service-to-service and user authentication.

**API Endpoints**

-   **POST /api/auth/login** --- Authenticate user and issue JWT token.
-   **POST /api/auth/roles** --- Manage user roles.

**JWT Claims**

-   Embeds user roles in **JWT claims** for easy validation.

* * * * *

### **6\. Viewer Service (Query Archived Data)**

**Responsibilities:**

-   Provides a **REST API** to view archived data.
-   **Enforces RBAC permissions** using JWT tokens.
-   Only allows access to **archived data** the user is authorized to view.

**API Endpoints**


-   **GET /api/view/{tableName}** --- View archived data from the archival database.

**Features:**

-   Uses **pagination and filtering** to manage large datasets.
-   **Applies RBAC rules** (only admin or authorized users can view specific tables).


**Data Flow (Sequence Diagram)**
--------------------------------

1.  **Admin Configures Rules**
    -   **Admin** → **Configuration Service** → **Save rules** in `archival_criteria` table.
2.  **Scheduler Triggers Archive Job**
    -   **Scheduler Service** → **Kafka** → **Archival Service**.
3.  **Archival Service Processes Data**
    -   **Archival Service** queries **Source Database**, transfers data to **Archival Database**.
4.  **Viewer Queries Archived Data**
    -   **User** → **Viewer Service** → **RBAC Validation** → **Query Archival Database**.

* * * * *

**Design Decisions**
--------------------

**Data Movement**

-   **Batch processing** for moving large datasets.
-   **ETL strategy** for large datasets, with batch size of **1000--5000 rows**.

**Security**

-   **RBAC for Table Access** --- Access control for different users.
-   **JWT for Authentication** --- Stateless token-based authentication.

**Data Deletion**

-   Ensures **safe, batch-by-batch deletion**.
-   **Idempotent Jobs** --- Use unique tokens for tracking archive and delete jobs.

**Scalability**

-   **Decouples Scheduler, Archival, Deletion** via Kafka event-driven system.

* * * * *

**Technologies Used**
---------------------

-   **Spring Boot** --- For microservices development.
-   **PostgreSQL** --- As the source and archival databases.
-   **Kafka** --- For event-driven communication.
-   **JWT** --- For user authentication and service-to-service authorization.
-   **Encryption ** --- AES Encryption

* * * * *

**Setup Instructions**
----------------------

**Clone the Repository**

bash

Copy code

`git clone https://github.com/your-repo/data-archival-system.git`

**Start Services Using Docker Compose**

bash

Copy code

`docker-compose up -d`


