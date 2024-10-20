plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.serialization")
    kotlin("plugin.spring")
}

dependencies {
    implementation("at.favre.lib:bcrypt:0.9.0")

    api(projects.apiSpecification.api)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation(projects.crossCuttingConcern.application.serverEvent)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation("com.auth0:java-jwt:3.18.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    val kotlinxSerializationVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}
