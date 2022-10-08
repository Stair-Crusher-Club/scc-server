plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    val kotlinLoggingVersion: String by project

    implementation(project(":stdlib"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
