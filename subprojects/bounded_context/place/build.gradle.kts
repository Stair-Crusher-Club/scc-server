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

    outputAdapterImplementation("org.springframework.boot:spring-boot-starter-web")
    outputAdapterImplementation("org.springframework:spring-webflux")
    outputAdapterImplementation("io.projectreactor.netty:reactor-netty")

    outputAdapterImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    outputAdapterImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    outputAdapterImplementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
}
