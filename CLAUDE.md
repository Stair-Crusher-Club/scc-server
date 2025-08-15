# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the SCC Server (계단정복지도/Stair Crusher Club) repository - a monolith Spring Boot application for managing accessibility information for buildings and places in South Korea. The codebase follows Domain-Driven Design (DDD) principles with hexagonal architecture.

## Essential Development Commands

### Build & Run
```bash
cd app-server

# Initialize submodules (required for API specs)
git submodule init && git submodule update

# Generate API code from OpenAPI specs
./gradlew openApiGenerate

# Start database
docker compose up -d

# Build the project
./gradlew build

# Run tests (layered: unitTest, integrationTest)
./gradlew test          # All tests
./gradlew unitTest      # Unit tests only
./gradlew integrationTest # Integration tests only

# Lint/Format (Detekt for Kotlin)
./gradlew detekt
```

### API Specification
```bash
cd app-server/subprojects/api_specification/scc-api

# Format API specs
npm run format:fix
npm run format:check
npm run lint
```

## Architecture Overview

### Module Structure
The project is organized into Gradle modules under `app-server/subprojects/`:

**API Specification Modules:**
- `api_specification/api` - Main client API specs
- `api_specification/admin_api` - Admin API specs  
- `api_specification/domain_event` - Domain event specifications

**Bounded Context Modules (DDD):**
Each bounded context follows hexagonal architecture with `domain/application/infra` layers:
- `bounded_context/challenge` - Challenge management
- `bounded_context/external_accessibility` - External accessibility data
- `bounded_context/misc` - Miscellaneous features (banners, etc.)
- `bounded_context/notification` - Notification system
- `bounded_context/place` - Places, buildings, accessibility info & search
- `bounded_context/quest` - Offline club quests
- `bounded_context/user` - User accounts & authentication

**Cross-cutting Concerns:**
- `cross_cutting_concern/stdlib` - Common utilities across all bounded contexts
- `cross_cutting_concern/domain/server_event` - Server event logging
- `cross_cutting_concern/infra/persistence_model` - DB migrations & JPA config
- `cross_cutting_concern/infra/spring_web` - Spring MVC utilities
- `cross_cutting_concern/infra/spring_message` - Domain event pub/sub system

**Deployment:**
- `deploying_apps/scc_server` - Main monolith server artifact
- `deploying_apps/local_script` - Local development scripts

### Inter-Bounded Context Communication

1. **Direct Method Calls**: For tightly coupled operations, bounded contexts can directly call other BC's application layer methods
2. **Domain Events**: For loose coupling, use domain event publish/subscribe pattern via `DomainEventPublisher` and `DomainEventSubscriber` from stdlib

### Test Strategy
- **Unit Tests** (`unitTest`): No `@SpringBootTest` - fast, isolated tests
- **Integration Tests** (`integrationTest`): Full Spring context tests
- Test layers are hierarchical - higher layers can access lower layer outputs

### Technology Stack
- **Language**: Kotlin with Java 19
- **Framework**: Spring Boot with Spring Data JPA
- **Database**: PostgreSQL (via Docker Compose)
- **Build**: Gradle with Kotlin DSL
- **API**: OpenAPI 3.0 specifications
- **Linting**: Detekt for Kotlin code style

### Key Development Guidelines
- Follow hexagonal architecture: domain ← application ← infra
- Use domain events for cross-BC communication when possible
- Keep bounded contexts isolated via Gradle modules
- Generate API clients from OpenAPI specs in scc-api submodule
- Database migrations are managed via Flyway in `persistence_model` module
- Always add trailing comma.
- **MANDATORY**: When evolving entities and database schemas, ensure backward compatibility - existing data must deserialize correctly after changes.

### File Structure Navigation
- Core business logic: `app-server/subprojects/bounded_context/{bc_name}/{layer}/src/main/kotlin/`
- Database migrations: `app-server/subprojects/cross_cutting_concern/infra/persistence_model/src/main/resources/db/migration/`
- API specifications: `app-server/subprojects/api_specification/scc-api/*.yaml`
- Infrastructure: `infra/` (Terraform & Helm charts)