# 🏦 ClusteredData Warehouse

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)

**Enterprise-Grade Spring Boot Application for Managing Foreign Exchange Deals**

</div>

---

## 🌟 Overview

**ClusteredData Warehouse** is a production-ready Spring Boot application designed for importing, managing, and querying Foreign Exchange (FX) deals. Built with enterprise best practices, it provides a robust RESTful API with comprehensive validation, duplicate detection, and interactive documentation.

### 🎯 Key Highlights

- **🔌 RESTful API**: Full CRUD operations with clean, organized endpoints
- **📁 CSV Bulk Import**: Process thousands of deals with detailed error reporting
- **🔍 Advanced Querying**: Pagination, sorting, and filtering capabilities
- **✅ Strict Validation**: ISO 4217 currencies, ISO 8601 timestamps, positive amounts
- **🚫 Duplicate Protection**: Two-level safeguards (application + database)
- **📚 Interactive Documentation**: Swagger UI for API exploration
- **🐳 Docker Ready**: One-command deployment with Docker Compose
- **🧪 Well Tested**: 70%+ code coverage with comprehensive test suite
- **⚡ Production Optimized**: Proper transaction management with immediate flush

---

## ✨ Features

### 🔌 REST API Endpoints

<table>
<tr>
<td>

**Deal Management**
- ✅ Create single deal
- ✅ Create batch deals
- ✅ Get deal by ID
- ✅ List all deals (paginated)
- ✅ Get statistics

</td>
<td>

**CSV Import**
- 📁 Bulk CSV upload
- ✅ Real-time validation
- 📊 Detailed error reports
- 🔄 Partial success support
- 📈 Import statistics

</td>
<td>

**Quality Features**
- 🛡️ Input trimming
- 🔍 Strict validation
- 🚫 Duplicate detection
- ⚡ Immediate DB flush
- 📝 Structured logging

</td>
</tr>
</table>

### 🛡️ Data Validation & Integrity

| Feature | Implementation | Details |
|---------|---------------|---------|
| **Input Sanitization** | Automatic field trimming | All string inputs trimmed before processing |
| **Currency Validation** | ISO 4217 enforcement | Valid codes only (USD, EUR, GBP, etc.) |
| **Blacklist Filtering** | Reject non-tradeable codes | XXX, XTS, XAU, XAG, XPT, XPD blocked |
| **Timestamp Format** | ISO 8601 standard | `yyyy-MM-dd'T'HH:mm:ss` format required |
| **Amount Validation** | Positive decimals only | String-based validation for precision |
| **Duplicate Detection** | Two-level protection | App-level check + DB unique constraint |
| **Transaction Safety** | Immediate flush | `flush()` after save for consistency |

### 📚 Documentation & Tools

- **Swagger UI**: Interactive API docs at `/swagger-ui.html`
- **OpenAPI Spec**: JSON specification at `/api-docs`
- **Postman Collection**: Complete test collection with examples
- **Enhanced Makefile**: 15+ commands for dev/test/deploy workflows

---

## 🛠 Tech Stack

<div align="center">

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Language** | Java | 17 | Modern JDK with LTS support |
| **Framework** | Spring Boot | 3.2.0 | Enterprise application framework |
| **Database** | PostgreSQL | 16 | ACID-compliant RDBMS |
| **ORM** | Spring Data JPA | 6.x | Object-relational mapping |
| **Migration** | Liquibase | 4.x | Database version control |
| **API Docs** | Springdoc OpenAPI | 2.3.0 | Interactive documentation |
| **Build** | Maven | 3.9+ | Dependency management |
| **Container** | Docker | Latest | Containerization |

</div>

**Testing Stack:**
- JUnit 5, Mockito, MockMvc
- JaCoCo (70%+ coverage enforced)
- H2 Database (in-memory testing)

---

## 🚀 Quick Start

### Prerequisites

- **Java 17+** ([Download](https://adoptium.net/))
- **Docker & Docker Compose** ([Download](https://www.docker.com/products/docker-desktop))
- **Optional**: Maven 3.9+ for local development

### 🐳 Docker Deployment (Recommended)

**Start everything in 2 commands:**

```bash
git clone https://github.com/theshamkhi/ProgressSoftAssignment.git
cd ProgressSoftAssignment
make docker-up
```

**Application URLs:**
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/api/deals/health
- **Database**: localhost:5433 (PostgreSQL)

---

## 📡 API Documentation

### Interactive Documentation

**Swagger UI**: http://localhost:8080/swagger-ui.html

<div align="center">
  <img src="https://img.shields.io/badge/Swagger-Try%20It%20Out-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger"/>
</div>

### Complete API Reference

#### 📝 Deal Management Endpoints

##### 1. Create Single Deal
```http
POST /api/deals
Content-Type: application/json

{
  "dealId": "DEAL001",
  "fromCurrency": "USD",
  "toCurrency": "EUR",
  "dealTimestamp": "2025-01-15T10:30:00",
  "dealAmount": "1000.50"
}
```

**Success Response (201 Created):**
```json
{
  "dealId": "DEAL001",
  "fromCurrency": "USD",
  "toCurrency": "EUR",
  "dealTimestamp": "2025-01-15T10:30:00",
  "dealAmount": 1000.50,
  "createdAt": "2025-01-15T10:31:23"
}
```

**Error Response (409 Conflict - Duplicate):**
```json
{
  "timestamp": "2025-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Deal with ID 'DEAL001' already exists",
  "path": "/api/deals"
}
```

---

##### 2. Create Batch Deals
```http
POST /api/deals/batch
Content-Type: application/json

{
  "deals": [
    {
      "dealId": "DEAL200",
      "fromCurrency": "USD",
      "toCurrency": "EUR",
      "dealTimestamp": "2025-01-15T10:30:00",
      "dealAmount": "1000.50"
    },
    {
      "dealId": "DEAL201",
      "fromCurrency": "GBP",
      "toCurrency": "USD",
      "dealTimestamp": "2025-01-15T11:00:00",
      "dealAmount": "2500.75"
    }
  ]
}
```

**Response (200 OK):**
```json
{
  "totalRecords": 2,
  "successfulRecords": 2,
  "failedRecords": 0,
  "duplicateRecords": 0,
  "errors": [],
  "warnings": []
}
```

---

##### 3. Get All Deals (Paginated)
```http
GET /api/deals?page=0&size=20&sortBy=createdAt&direction=DESC
```

---

##### 4. Get Deal by ID
```http
GET /api/deals/{dealId}
```

**Example:**
```bash
curl http://localhost:8080/api/deals/DEAL001
```

---

##### 5. Get Total Count
```http
GET /api/deals/count
```

**Response:**
```json
150
```

---

#### 📥 CSV Import Endpoint

##### Import Deals from CSV
```http
POST /api/deals/csv/import
Content-Type: multipart/form-data

file: <CSV file>
```

**CSV Format:**
```csv
dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
DEAL002,GBP,JPY,2025-01-15T11:00:00,2500.75
```

**Success Response:**
```json
{
  "totalRecords": 2,
  "successfulRecords": 2,
  "failedRecords": 0,
  "duplicateRecords": 0,
  "errors": [],
  "warnings": []
}
```

**Partial Success Response:**
```json
{
  "totalRecords": 3,
  "successfulRecords": 2,
  "failedRecords": 1,
  "duplicateRecords": 0,
  "errors": [
    "Row 3: Invalid From Currency code: 'XXX' is not a valid ISO 4217 currency."
  ],
  "warnings": []
}
```

---

#### 💚 Health Check
```http
GET /api/deals/health
```

**Response:**
```
ClusteredData Warehouse is running
```

---

## 🧪 Testing

### Run Tests

```bash
# All tests
make test

# With coverage
make coverage

# Specific test
mvn test -Dtest=ValidationUtilTest
```

### Test Coverage

- **Overall**: 70%+
- **60+ Unit Tests**
- **Integration Tests**

**Coverage by Layer:**
- ✅ Controllers: 85%+
- ✅ Services: 90%+

View report: `target/site/jacoco/index.html`

---

## 🐳 Deployment

### Docker Commands

```bash
# Start everything
make docker-up

# View logs
make logs

# Restart
make docker-restart

# Full reset
make docker-reset

# Rebuild from scratch
make docker-rebuild
```
---

## 🔧 Development

### Makefile Commands

```bash
# Build Commands
make build          # Build with Maven
make clean          # Clean artifacts
make verify         # Full verification (build + test + coverage)

# Testing Commands
make test           # Run all tests
make coverage       # Generate coverage report

# Local Commands
make run            # Run locally

# Docker Commands
make docker-build   # Build image
make docker-up      # Start containers
make docker-down    # Stop containers
make docker-restart # Restart
make docker-sync    # Sync code changes (no rebuild)
make docker-reset   # Full reset
make docker-rebuild # Rebuild no cache

# Debug Commands
make logs           # App logs
make logs-db        # DB logs
make logs-all       # All logs
make db-shell       # PostgreSQL shell
make health         # Health check
```

### Quick Development Workflow

```bash
# 1. Make code changes

# 2. Sync to container (fast)
make docker-sync

# 3. Test
make health
curl http://localhost:8080/api/deals

# 4. View logs
make logs
```

---

## 📁 Project Structure

```
clustered-data-warehouse/
│
├── src/
│   ├── main/
│   │   ├── java/com/progressoft/fxdeals/
│   │   │   ├── config/
│   │   │   │   └── OpenAPIConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── DealController.java
│   │   │   │   └── ImportController.java
│   │   │   ├── dto/
│   │   │   │   ├── CreateDealRequest.java
│   │   │   │   ├── BatchCreateRequest.java
│   │   │   │   ├── DealResponse.java
│   │   │   │   ├── DealDTO.java
│   │   │   │   ├── ImportResultDTO.java
│   │   │   │   └── ErrorResponse.java
│   │   │   ├── service/
│   │   │   │   ├── DealService.java
│   │   │   │   ├── DealServiceImpl.java
│   │   │   │   ├── CSVImporterService.java
│   │   │   │   └── CSVImporterServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── DealRepository.java
│   │   │   ├── model/
│   │   │   │   └── Deal.java
│   │   │   ├── mapper/
│   │   │   │   └── DealMapper.java
│   │   │   ├── util/
│   │   │   │   └── ValidationUtil.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ValidationException.java
│   │   │   │   └── DuplicateRecordException.java
│   │   │   └── FxDealsApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/changelog/
│   └── test/
│       └── java/com/progressoft/fxdeals/
│           ├── controller/
│           └── service/
│
├── docker-compose.yml
├── Dockerfile
├── Makefile
├── pom.xml
├── API.postman_collection.json
└── README.md
```

---

<div align="center">

**⭐ Star this repository if you find it helpful!**

</div>