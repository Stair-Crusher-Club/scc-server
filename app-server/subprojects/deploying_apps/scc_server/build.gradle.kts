import com.google.cloud.tools.jib.api.buildplan.ImageFormat.Docker

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.jib)
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework:spring-jdbc")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("com.zaxxer:HikariCP")
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.bundles.sentry)
    runtimeOnly(libs.bundles.logback)

    implementation(projects.crossCuttingConcern.infra.springWeb)
    implementation(projects.crossCuttingConcern.infra.springMessage)
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation(projects.crossCuttingConcern.infra.messageQueue)
}

jib {
    containerizingMode = "packaged"
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
        image = "eclipse-temurin:19"
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
        credHelper.helper = "ecr-login"
        val version = property("version") as? String ?: throw IllegalArgumentException("No property `version` exists!")
        image = "public.ecr.aws/i6n1n6v2/scc-server:$version"
    }
    container {
        entrypoint = listOf("/app/run-java.sh")
        environment = mapOf(
            "JAVA_MAIN_CLASS" to "@/app/jib-main-class-file",
            "JAVA_CLASSPATH" to "@/app/jib-classpath-file",
        )
        ports = listOf("8080", "18080")
        format = Docker
    }
}
