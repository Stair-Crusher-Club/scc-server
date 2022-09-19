plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    infraImplementation("org.springframework.boot:spring-boot-starter-web")
    infraApi(project(":admin-api"))
}
