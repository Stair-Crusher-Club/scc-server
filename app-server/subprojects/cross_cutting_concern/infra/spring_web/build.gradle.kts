plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

tasks.register<Copy>("copySccOpenApiSpecs") {
    from(
        "${rootProject.projectDir}/subprojects/api_specification/scc-api/api-spec.yaml",
        "${rootProject.projectDir}/subprojects/api_specification/scc-api/admin-api-spec.yaml"
    )
    into(layout.buildDirectory.dir("resources/main"))
}

tasks.named("processResources") {
    dependsOn("copySccOpenApiSpecs")
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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.java.jwt)
    implementation(libs.guava)
    implementation(libs.kotlin.logging)
    implementation(libs.jackson.module.kotlin)
    runtimeOnly(libs.coroutines.reactor)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    integrationTestImplementation(libs.jackson.module.kotlin)
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation(projects.crossCuttingConcern.application.serverEvent)
}
