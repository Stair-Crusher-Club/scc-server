plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    api(projects.adminApi)
    implementation(projects.stdlib)
    implementation(projects.boundedContext.user.application)

    implementation("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")
    val kotlinLoggingVersion: String by project
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val jacksonModuleKotlinVersion: String by project
    integrationTestImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
}
