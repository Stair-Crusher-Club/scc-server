plugins {
    kotlin("plugin.serialization")
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    val kotlinLoggingVersion: String by project
    val kotlinSerializationVersion: String by project
    val jakartaInjectVersion: String by project

    applicationImplementation("jakarta.inject:jakarta.inject-api:$jakartaInjectVersion")

    infraImplementation("org.springframework.boot:spring-boot-starter-web")
    infraImplementation("org.springframework:spring-webflux")
    infraImplementation("io.projectreactor.netty:reactor-netty")

    infraImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    infraImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    infraImplementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
}
