# SCC Server Development Guidelines

This document provides essential information for developers working on the SCC Server project.

## Build/Configuration Instructions

### Project Structure

The project follows a modular monolith architecture with bounded contexts:

- Each bounded context is organized into three layers:
  - `domain`: Contains domain models and business logic
  - `application`: Contains use cases and application services
  - `infra`: Contains infrastructure code (controllers, repositories, etc.)

- Cross-cutting concerns are separated into their own modules:
  - `stdlib`: Core utilities and domain primitives
  - `application`: Application-level abstractions
  - `infra`: Infrastructure implementations
  - `test`: Testing utilities

### Build System

The project uses Gradle with Kotlin DSL for build configuration:

```bash
# Build the entire project
./gradlew build

# Build a specific module
./gradlew :subprojects:bounded_context:place:infra:build
```

### Running the Application

```bash
# Run the server application
./gradlew :subprojects:deploying_apps:scc_server:bootRun
```

### Docker Setup

The project includes Docker configuration for local development:

```bash
# Start the required services (database, etc.)
docker-compose up -d
```

## Testing Information

### Test Structure

Tests are organized into two layers:

1. **Unit Tests**: Located in `src/unitTest/kotlin` directories
   - Do not use Spring context
   - Focus on testing individual components in isolation
   - Faster execution

2. **Integration Tests**: Located in `src/integrationTest/kotlin` directories
   - Use Spring context with `@SpringBootTest`
   - Test interactions between components
   - May use mock implementations of external services

### Running Tests

```bash
# Run all tests
./gradlew test

# Run only unit tests
./gradlew unitTest

# Run only integration tests
./gradlew integrationTest

# Run tests for a specific module
./gradlew :subprojects:bounded_context:place:infra:test
./gradlew :subprojects:bounded_context:place:infra:unitTest
./gradlew :subprojects:bounded_context:place:infra:integrationTest
```

### Writing Tests

#### Unit Test Example

Here's a simple unit test example:

```kotlin
class StringUtilsTest {
    @Test
    fun `reverse - should reverse the input string`() {
        // Given
        val input = "hello"
        val expected = "olleh"

        // When
        val result = StringUtils.reverse(input)

        // Then
        assertEquals(expected, result)
    }
}
```

#### Integration Test Example

For integration tests that require Spring context:

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class ExampleControllerTest {
    @Autowired
    lateinit var mvc: MockMvc

    @Test
    fun `should return expected response`() {
        mvc
            .get("/api/example")
            .andExpect {
                status { isOk() }
                content { json("""{"result":"success"}""") }
            }
    }
}
```

### Test Base Classes

Many bounded contexts have base test classes that provide common functionality:

- `*ITBase` classes for integration tests
- Mock implementations for external services

## Additional Development Information

### Code Style

- The project uses Detekt for static code analysis
- Run `./gradlew detekt` to check for code style issues
- Configuration is in `detekt-config.yml`
- Several rules are disabled in the configuration to accommodate project-specific needs:
  - Complexity rules like `ComplexCondition`, `LongParameterList`, `TooManyFunctions`
  - Style rules like `ForbiddenComment`, `MaxLineLength`, `MagicNumber`
  - Naming rules like `PackageNaming`, `InvalidPackageDeclaration`

### API Code Generation

- The project uses OpenAPI Generator to generate API interfaces and DTOs from OpenAPI specifications
- API specifications are located in `subprojects/api_specification/api/scc-api/api-spec.yaml` and `subprojects/api_specification/admin_api/scc-api/api-spec.yaml`
- Run `./gradlew openApiGenerate` to generate code from the API specifications
- Generated code is placed in `src/main/kotlin` directories of the respective modules
- The generator is configured to use Jackson for serialization
- The `openApiGenerate` task runs automatically before Kotlin compilation
- When adding new API endpoints or DTOs, update the API specification files and run the generator

### Database Migration Scripts

- The project uses Flyway for database schema migrations
- Migration scripts are located in `subprojects/cross_cutting_concern/infra/persistence_model/src/main/resources/db/migration`
- Scripts follow the naming convention `V{version}__{description}.sql` where:
  - `{version}` is a sequential number (e.g., V1, V2, V42)
  - `{description}` is a brief description of the migration using underscores (e.g., add_user_table)
- Migrations are automatically applied when the application starts
- Each script should be idempotent (safe to run multiple times)
- Types of migrations:
  - Creating new tables: `CREATE TABLE IF NOT EXISTS table_name (...)`
  - Adding columns: `ALTER TABLE table_name ADD COLUMN column_name ...`
  - Creating indexes: `CREATE INDEX idx_name ON table_name(column_name)`
  - Other schema changes as needed
- Best practices:
  - Keep migrations small and focused on a single change
  - Use descriptive names that clearly indicate the purpose
  - Include `IF NOT EXISTS` or similar guards when appropriate
  - Add comments for complex changes
  - Test migrations thoroughly before deployment
  - Never modify an existing migration that has been applied to any environment

### Dependency Injection

- The project uses Spring's dependency injection
- Domain and application layers should not depend on Spring
- Use constructor injection for dependencies

### Transaction Management

- Transactions are managed at the application service level
- Use the `transactionManager.doInTransaction` with appropriate isolation level.

### Error Handling

- Domain exceptions should extend `SccDomainException`
- The `SccExceptionHandler` handles exceptions and converts them to appropriate HTTP responses

### Logging

- Use SLF4J for logging
- Log levels should be appropriate for the context (DEBUG for detailed information, INFO for general information, etc.)

### Adding New Features

1. Define the domain model and business logic in the domain layer
2. Create use cases in the application layer
3. Implement infrastructure components in the infra layer
4. Add tests for all layers
5. Update API specifications if needed
