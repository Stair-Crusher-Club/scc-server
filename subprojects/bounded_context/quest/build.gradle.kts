plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    infraImplementation("org.springframework.boot:spring-boot-starter-web")
    infraImplementation(project(":bounded_context:place", "domain"))
    infraImplementation(project(":bounded_context:place", "application"))
    infraImplementation(project(":bounded_context:accessibility", "domain"))
    infraImplementation(project(":bounded_context:accessibility", "application"))
    infraApi(project(":admin-api"))

    testImplementation(project(":bounded_context:quest", "infra"))
}
