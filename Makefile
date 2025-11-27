.PHONY: help build test clean run docker docker-up docker-down docker-reset docker-rebuild docker-logs coverage

help:
	@echo "FX Deals Importer - Available Commands"
	@echo "======================================="
	@echo "make build        - Build the project with Maven"
	@echo "make test         - Run unit tests"
	@echo "make coverage     - Generate test coverage report"
	@echo "make run          - Run the application locally"
	@echo "make clean        - Clean build artifacts"
	@echo "make docker       - Build Docker image"
	@echo "make docker-up    - Start Docker containers"
	@echo "make docker-down  - Stop Docker containers"
	@echo "make docker-logs  - View Docker container logs"
	@echo "make docker-reset - Stop containers and remove volumes"
	@echo "make docker-rebuild - Rebuild from scratch"

build:
	@echo "Building the project..."
	mvn clean package -DskipTests

test:
	@echo "Running tests..."
	mvn test

coverage:
	@echo "Generating coverage report..."
	mvn clean test jacoco:report
	@echo "Coverage report available at: target/site/jacoco/index.html"

run:
	@echo "Running application..."
	mvn spring-boot:run

clean:
	@echo "Cleaning build artifacts..."
	mvn clean
	@if [ -d "logs" ]; then \
		echo "Removing logs directory..."; \
		rm -rf logs/; \
	else \
		echo "No logs directory to remove."; \
	fi

docker:
	@echo "Building Docker image..."
	docker build -t fxdeals:latest .

docker-up:
	@echo "Starting Docker containers..."
	docker-compose up -d
	@echo "Application starting... Wait for healthcheck to pass"
	@echo "Access the application at: http://localhost:8080"

docker-down:
	@echo "Stopping Docker containers..."
	docker-compose down

docker-reset:
	@echo "Stopping and removing containers, volumes, and orphans..."
	docker-compose down --volumes --remove-orphans
	@echo "Pruning dangling containers and images..."
	docker container prune -f
	docker image prune -f
	@echo "Removing logs directory..."
	@if [ -d "logs" ]; then rm -rf logs/; fi
	@echo "Full Docker reset completed."

docker-rebuild:
	@echo "Rebuilding Docker images from scratch..."
	docker-compose down
	docker-compose build --no-cache
	@echo "Starting containers..."
	docker-compose up -d
	@echo "Rebuild complete. Application available at http://localhost:8080"

docker-logs:
	@echo "Showing container logs..."
	docker-compose logs -f app

docker-logs-all:
	@echo "Showing all container logs..."
	docker-compose logs -f