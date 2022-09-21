plugins {
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

    outputAdapterImplementation("org.springframework.boot:spring-boot-starter-web")
    outputAdapterImplementation("org.springframework:spring-webflux")
    outputAdapterImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    outputAdapterImplementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    outputAdapterImplementation("io.projectreactor.netty:reactor-netty")
}
