.PHONY: help build test coverage clean run docker-build docker-up docker-down docker-restart docker-sync docker-reset docker-rebuild logs logs-db logs-all db-shell health verify

.DEFAULT_GOAL := help

help:
	@echo "FX Deals Importer - Available Commands"
	@echo ""
	@echo "Build Commands:"
	@echo "  build          - Build the application with Maven"
	@echo "  clean          - Clean build artifacts and logs"
	@echo "  verify         - Run full verification (build + test + coverage)"
	@echo ""
	@echo "Testing Commands:"
	@echo "  test           - Run all tests"
	@echo "  coverage       - Run tests with coverage report"
	@echo ""
	@echo "Local Commands:"
	@echo "  run            - Run application locally"
	@echo ""
	@echo "Docker Commands:"
	@echo "  docker-build   - Build Docker image"
	@echo "  docker-up      - Start all containers"
	@echo "  docker-down    - Stop and remove containers"
	@echo "  docker-restart - Restart all containers"
	@echo "  docker-sync    - Sync code changes to container (no rebuild)"
	@echo "  docker-reset   - Stop containers and remove volumes"
	@echo "  docker-rebuild - Rebuild from scratch (no cache)"
	@echo ""
	@echo "Debug Commands:"
	@echo "  logs           - View application logs"
	@echo "  logs-db        - View database logs"
	@echo "  logs-all       - View all logs"
	@echo "  db-shell       - Open PostgreSQL shell"
	@echo "  health         - Check application health"
	@echo ""

build:
	@echo "Building application..."
	mvn clean package -DskipTests
	@echo "Build completed successfully"

test:
	@echo "Running tests..."
	mvn test
	@echo "Tests completed"

coverage:
	@echo "Running tests with coverage..."
	mvn clean test jacoco:report
	@echo "Coverage report generated: target/site/jacoco/index.html"

clean:
	@echo "Cleaning build artifacts..."
	mvn clean
	@if [ -d "logs" ]; then \
		echo "Removing logs directory..."; \
		rm -rf logs/; \
	fi
	@echo "Cleaned successfully"

run:
	@echo "Running application locally..."
	mvn spring-boot:run

docker-build:
	@echo "Building Docker image..."
	docker-compose build
	@echo "Docker image built successfully"

docker-up:
	@echo "Starting containers..."
	docker-compose up -d
	@echo "Containers started successfully"
	@echo "Application starting... Wait for healthcheck to pass"
	@echo "API: http://localhost:8080"

docker-down:
	@echo "Stopping containers..."
	docker-compose down
	@echo "Containers stopped successfully"

docker-restart: docker-down docker-up

docker-sync:
	@echo "Syncing code changes to container..."
	@echo "Step 1/3: Rebuilding JAR..."
	mvn clean package -DskipTests
	@echo "Step 2/3: Copying JAR to container..."
	docker cp target/fxdeals-0.0.1-SNAPSHOT.jar fxdeals-app:/app/app.jar
	@echo "Step 3/3: Restarting application..."
	docker-compose restart app
	@echo "Changes synced successfully - app restarted"

docker-reset:
	@echo "Stopping and removing containers, volumes, and orphans..."
	docker-compose down --volumes --remove-orphans
	@echo "Pruning dangling containers and images..."
	docker container prune -f
	docker image prune -f
	@if [ -d "logs" ]; then \
		echo "Removing logs directory..."; \
		rm -rf logs/; \
	fi
	@echo "Full Docker reset completed"

docker-rebuild:
	@echo "Rebuilding Docker images from scratch..."
	docker-compose down
	docker-compose build --no-cache
	@echo "Starting containers..."
	docker-compose up -d
	@echo "Rebuild complete. Application available at http://localhost:8080"

logs:
	@echo "Viewing application logs..."
	docker-compose logs -f app

logs-db:
	@echo "Viewing database logs..."
	docker-compose logs -f postgres

logs-all:
	@echo "Viewing all logs..."
	docker-compose logs -f

db-shell:
	@echo "Opening PostgreSQL shell..."
	docker-compose exec postgres psql -U fxuser -d fxdeals

health:
	@echo "Checking application health..."
	@curl -f http://localhost:8080/api/deals/health || echo "Application not responding"

verify:
	@echo "Running full verification..."
	@echo "Step 1/3: Building..."
	@$(MAKE) build
	@echo "Step 2/3: Running tests..."
	@$(MAKE) test
	@echo "Step 3/3: Generating coverage report..."
	@$(MAKE) coverage
	@echo "Verification completed successfully"