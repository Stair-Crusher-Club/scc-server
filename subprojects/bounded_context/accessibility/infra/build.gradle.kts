plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":bounded_context:place:application"))

    api(project(":api"))
    implementation(projects.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")

    integrationTestImplementation(projects.testing.springIt)
}
