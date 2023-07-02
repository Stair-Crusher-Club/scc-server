plugins {
    kotlin("plugin.serialization")
    id("org.springframework.boot")
}

dependencies {
    val kotlinLoggingVersion: String by project
    val kotlinxSerializationVersion: String by project

    implementation(projects.stdlib)
    implementation(projects.persistenceModel)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val guavaVersion: String by project
    implementation("com.google.guava:guava:$guavaVersion")
}
