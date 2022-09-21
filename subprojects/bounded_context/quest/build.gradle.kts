plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    applicationImplementation("org.springframework:spring-context:5.3.22")

    infraImplementation("org.springframework.boot:spring-boot-starter-web")
    infraApi(project(":admin-api"))
}
