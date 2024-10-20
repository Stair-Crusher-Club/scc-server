plugins {
    kotlin("plugin.serialization")
    id("org.springframework.boot")
}

dependencies {
    val kotlinLoggingVersion: String by project
    val kotlinxSerializationVersion: String by project

    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation(projects.crossCuttingConcern.infra.network)
    implementation(projects.apiSpecification.domainEvent)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val guavaVersion: String by project
    implementation("com.google.guava:guava:$guavaVersion")

    testImplementation(projects.apiSpecification.domainEvent)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
}
