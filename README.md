# Spring Loom Demo

REST API project developed in Java using Spring Boot 3 that demonstrates scalable concurrent batch processing with Java 21 Project Loom (Virtual
Threads) by ingesting customer data from CSV files, mapping records to DTOs, and persisting them into MongoDB through concurrent upsert operations
with controlled parallelism using semaphores.

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

**Customer**

```javascript
db.customers.createIndex({ customerId: 1 }, { unique: true });
```

## Data Generator

[Customer Data Generator](https://github.com/erebelo/spring-loom-demo/blob/main/docs/customer-data-generator.md)

## Collection

[Project Collection](https://github.com/erebelo/spring-loom-demo/tree/main/collection)
