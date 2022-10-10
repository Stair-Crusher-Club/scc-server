plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.zaxxer:HikariCP")
    val postgresqlVersion: String by project
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")

    implementation(project(":spring-web"))
    implementation(project(":spring_message"))
    implementation(project(":stdlib"))
    implementation(projects.persistenceModel)
}

tasks.bootJar { enabled = false }
