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

    implementation(projects.springWeb)
    implementation(projects.springMessage)
    implementation(projects.stdlib)
    implementation(projects.persistenceModel)
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
        image = "public.ecr.aws/q0g6g7m8/scc-server"
        credHelper.helper = "ecr-login"
        credHelper.environment = mapOf("AWS_PROFILE" to "swann-scc")
        tags = setOf("latest")
    }
    container {
        entrypoint = listOf("./app/run-java.sh")
        environment = mapOf(
            "JAVA_MAIN_CLASS" to "club.staircrusher.packaging.SccServerApplicationKt",
            "JAVA_LIB_DIR" to "/app/libs/*:/app/classes:/app/resources",
        )
        mainClass = "club.staircrusher.packaging.SccServerApplicationKt"
        ports = listOf("8080", "18080")
        format = Docker
    }
}
