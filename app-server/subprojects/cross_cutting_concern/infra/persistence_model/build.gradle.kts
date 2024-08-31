plugins {
    idea
}

dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.boundedContext.accessibility.domain)
    implementation(projects.boundedContext.challenge.domain)
    implementation(projects.boundedContext.quest.domain)
    implementation(projects.boundedContext.user.domain)
    implementation(projects.boundedContext.externalAccessibility.domain)
    implementation(projects.crossCuttingConcern.domain.serverEvent)

    val kotlinLoggingVersion: String by project
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val flywayVersion: String by project
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    val jacksonModuleKotlinVersion: String by project
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
}
