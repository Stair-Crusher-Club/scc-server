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

    implementation(projects.stdlib)
    implementation(projects.persistenceModel)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val guavaVersion: String by project
    implementation("com.google.guava:guava:$guavaVersion")
}
