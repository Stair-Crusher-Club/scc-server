plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
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
    implementation(libs.guava)
    implementation(libs.kotlin.logging)
    implementation(libs.jackson.module.kotlin)
    runtimeOnly(libs.coroutines.reactor)

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    integrationTestImplementation(libs.jackson.module.kotlin)
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation(projects.crossCuttingConcern.application.serverEvent)
}
