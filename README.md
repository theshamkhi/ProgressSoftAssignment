# 💱 FX Deals Data Warehouse

A Spring Boot application for importing and persisting FX deal transactions from CSV files into a PostgreSQL database with comprehensive validation and error handling.

---

## 📖 Description

Data warehouse service that accepts FX deal details from CSV files, validates all fields, prevents duplicates, and provides detailed import results with row-level error reporting.

---

## 🛠 Tech Stack

- **Java 17** | **Spring Boot 3.2.0** | **PostgreSQL 16**
- **Spring Data JPA** | **Liquibase** | **Maven**
- **JUnit 5** | **Mockito** | **JaCoCo**
- **Docker** | **Docker Compose**

---

## ✨ Features

- ✅ ISO 4217 currency validation (rejects XXX, XTS, etc.)
- ✅ Timestamp validation (ISO 8601: `yyyy-MM-dd'T'HH:mm:ss`)
- ✅ Duplicate detection with unique deal IDs
- ✅ Row-level error reporting with line numbers
- ✅ Positive amount validation
- ✅ 61 comprehensive tests

---

## 🚀 Quick Start

### Prerequisites
- Java 17+, Maven 3.8+, Docker (optional)

### Run with Docker
```bash
make docker-up
```

### Run Locally
```bash
# Build and test
make build && make test

# Run application (requires PostgreSQL)
make run
```

Application runs at: **http://localhost:8080**

---

## 📡 API Usage

### Import CSV File
```bash
curl -X POST http://localhost:8080/api/deals/import \
  -F "file=@deals.csv"
```

**Response:**
```json
{
  "totalRecords": 10,
  "successfulRecords": 8,
  "failedRecords": 1,
  "duplicateRecords": 1,
  "errors": ["Row 5: Invalid From Currency code: 'XXX'"],
  "warnings": ["Row 9: Duplicate deal ID 'DEAL123'"]
}
```

### Health Check
```bash
curl http://localhost:8080/api/deals/health
```

---

## 📄 CSV Format

```csv
dealId,fromCurrency,toCurrency,dealTimestamp,dealAmount
DEAL001,USD,EUR,2025-01-15T10:30:00,1000.50
DEAL002,GBP,USD,2025-01-15T11:00:00,2500.75
```

**Requirements:**
- `dealId`: Unique identifier (required)
- `fromCurrency`: ISO 4217 code like USD, EUR (required)
- `toCurrency`: ISO 4217 code (required)
- `dealTimestamp`: Format `yyyy-MM-dd'T'HH:mm:ss` (required)
- `dealAmount`: Positive decimal number (required)

---

## 🧪 Testing

```bash
# Run tests
make test

# Generate coverage report
make coverage
```

**Test Coverage:**

| Component | Tests | Coverage |
|-----------|-------|----------|
| ValidationUtil | 22 | ~95% |
| Services | 18 | ~95% |
| Controller | 12 | ~90% |
| Mapper | 4 | ~100% |
| **Total** | **61** | **~85%** |

---

## 🐳 Docker Commands

```bash
make docker-up        # Start services
make docker-logs      # View logs
make docker-down      # Stop services
make docker-rebuild   # Rebuild from scratch
```

---

## 📁 Project Structure

```
src/main/java/com/progressoft/fxdeals/
├── controller/      # REST endpoints
├── service/         # Business logic
├── repository/      # Data access
├── model/           # JPA entities
├── dto/             # Data transfer objects
├── util/            # Validation
└── exception/       # Error handling
```
---

**Mohammed Shamkhi**