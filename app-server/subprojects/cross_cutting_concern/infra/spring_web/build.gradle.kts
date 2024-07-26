plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    api(projects.apiSpecification.adminApi)
    api(projects.apiSpecification.api)
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.boundedContext.user.application)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    api("org.springframework.boot:spring-boot-starter-security")
    val kotlinLoggingVersion: String by project
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val jacksonModuleKotlinVersion: String by project
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    integrationTestImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation(projects.crossCuttingConcern.application.serverEvent)
}
