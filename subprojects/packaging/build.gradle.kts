plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":admin-api"))
    implementation(project(":stdlib"))
    implementation(project(":bounded_context:quest", "domain"))
    implementation(project(":bounded_context:quest", "application"))
    implementation(project(":bounded_context:quest", "infra"))
}
