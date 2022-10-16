plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":stdlib"))
    implementation(project(":bounded_context:user:application"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")

    integrationTestImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
}
