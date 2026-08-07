.PHONY: help dev down frontend-dev frontend-check backend-dev backend-test

help:
	@echo "Medical Aid Tracker"
	@echo "  make dev            Start MySQL, backend, and frontend with containers"
	@echo "  make down           Stop local containers"
	@echo "  make frontend-dev   Start the Next.js development server"
	@echo "  make frontend-check Run frontend lint, tests, and production build"
	@echo "  make backend-dev    Start the Spring Boot API"
	@echo "  make backend-test   Run backend tests"

dev:
	docker compose up --build

down:
	docker compose down

frontend-dev:
	cd frontend && npm run dev

frontend-check:
	cd frontend && npm run lint && npm run typecheck && npm test && npm run build

backend-dev:
	cd backend && ./mvnw spring-boot:run

backend-test:
	cd backend && ./mvnw verify
