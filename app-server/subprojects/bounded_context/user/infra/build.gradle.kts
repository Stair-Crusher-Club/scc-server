plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("at.favre.lib:bcrypt:0.9.0")

    api(projects.apiSpecification.api)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation("com.auth0:java-jwt:3.18.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    val kotlinxSerializationVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
}
