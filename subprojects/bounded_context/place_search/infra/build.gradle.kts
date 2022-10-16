plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":bounded_context:place:application"))
    implementation(project(":bounded_context:accessibility:application"))
    val kotlinxCoroutinesReactorVersion: String by project
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxCoroutinesReactorVersion")

    api(project(":api"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    integrationTestImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    integrationTestImplementation(projects.testing.springIt)
}
