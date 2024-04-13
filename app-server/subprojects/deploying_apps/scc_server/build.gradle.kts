import com.google.cloud.tools.jib.api.buildplan.ImageFormat.Docker

plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("com.google.cloud.tools.jib")
}

dependencies {
    val postgresqlVersion: String by project

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework:spring-jdbc")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.zaxxer:HikariCP")
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")

    val sentryVersion: String by project
    runtimeOnly("io.sentry:sentry-spring-boot-starter-jakarta:$sentryVersion")
    runtimeOnly("io.sentry:sentry-logback:$sentryVersion")

    val logbackJsonVersion: String by project
    runtimeOnly("ch.qos.logback.contrib:logback-jackson:$logbackJsonVersion")
    runtimeOnly("ch.qos.logback.contrib:logback-json-classic:$logbackJsonVersion")

    implementation(projects.crossCuttingConcern.infra.springWeb)
    implementation(projects.crossCuttingConcern.infra.springMessage)
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
}

jib {
    extraDirectories {
        paths {
            path {
                this.setFrom("$rootDir/scripts")
                into = "/app"
            }
        }
        permissions.put("/app/run-java.sh", "755")
    }
    from {
        image = "openjdk:19"
        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }
    to {
        image = "public.ecr.aws/i6n1n6v2/scc-server"
        credHelper.helper = "ecr-login"
        val version = property("version") as? String ?: throw IllegalArgumentException("No property `version` exists!")
        tags = setOf(version)
    }
    container {
        entrypoint = listOf("./app/run-java.sh")
        environment = mapOf(
            "JAVA_MAIN_CLASS" to "club.staircrusher.scc_server.SccServerApplicationKt",
            "JAVA_LIB_DIR" to "/app/libs/*:/app/classes:/app/resources",
        )
        mainClass = "club.staircrusher.scc_server.SccServerApplicationKt"
        ports = listOf("8080", "18080")
        format = Docker
    }
}
