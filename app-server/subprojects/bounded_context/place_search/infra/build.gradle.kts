plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.accessibility.domain)
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    api(project(":api"))
    implementation("org.springframework.boot:spring-boot-starter-web")

    integrationTestImplementation(projects.boundedContext.place.application)
    integrationTestImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    integrationTestImplementation(projects.testing.springIt)
}
