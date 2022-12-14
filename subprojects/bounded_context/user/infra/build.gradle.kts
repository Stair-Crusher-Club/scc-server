plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("at.favre.lib:bcrypt:0.9.0")

    api(project(":api"))
    implementation(projects.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")

    integrationTestImplementation(projects.testing.springIt)
}
