# Spring Loom Demo

REST API developed with Java 21 and Spring Boot 4 that demonstrates scalable concurrent batch processing using Project Loom (Virtual Threads).
The application ingests customer data from CSV files, maps records to DTOs, and performs concurrent upsert operations into MongoDB while controlling parallelism with semaphores.
The processing pipeline is extensible through the Strategy design pattern, allowing different batch processors to be registered and orchestrated transparently.

## Requirements

- Java 21
- Spring Boot 4.x.x
- Apache Maven 3.8.6

## Libraries

- [spring-common-parent](https://github.com/erebelo/spring-common-parent): Manages the Spring Boot version and provide common configurations for
  plugins and formatting.

## Configuring Maven for GitHub Dependencies

To pull the `spring-common-parent` dependency, follow these steps:

1. **Generate a Personal Access Token**:

   Go to your GitHub account -> **Settings** -> **Developer settings** -> **Personal access tokens** -> **Tokens (classic)** -> **Generate new token (
   classic)**:
   - Fill out the **Note** field: `Pull packages`.
   - Set the scope:
     - `read:packages` (to download packages)
   - Click **Generate token**.

2. **Set Up Maven Authentication**:

   In your local Maven `settings.xml`, define the GitHub repository authentication using the following structure:

   ```xml
   <servers>
     <server>
       <id>github-spring-common-parent</id>
       <username>USERNAME</username>
       <password>TOKEN</password>
     </server>
   </servers>
   ```

   **NOTE**: Replace `USERNAME` with your GitHub username and `TOKEN` with the personal access token you just generated.

## Run App

- Create the required MongoDB indexes described in the [MongoDB Indexes](#mongodb-indexes) section.
- Complete the required [Data Generator](#data-generator) step.
- Run the `SpringLoomDemoApplication` class as Java Application.

## MongoDB Indexes

Creating the required indexes is essential for good batch processing performance.

**batch_executions:**

```javascript
// Ensures only one RUNNING execution exists per processor.
db.batch_executions.createIndex(
  { processor: 1 },
  { unique: true, partialFilterExpression: { status: "RUNNING" } },
);
```

**customers:**

```javascript
db.customers.createIndex({ customerId: 1 }, { unique: true });
```

## Data Generator

[Customer Data Generator](https://github.com/erebelo/spring-loom-demo/blob/main/docs/customer-data-generator.md)

## Collection

[Project Collection](https://github.com/erebelo/spring-loom-demo/tree/main/collection)
