plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation("com.auth0:java-jwt:3.18.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")

    api(project(":api"))
    implementation(projects.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.bootJar { enabled = false }
