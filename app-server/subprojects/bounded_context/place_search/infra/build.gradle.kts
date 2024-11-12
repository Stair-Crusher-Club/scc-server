plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.accessibility.domain)
    runtimeOnly(libs.coroutines.reactor)

    api(projects.apiSpecification.api)
    implementation("org.springframework.boot:spring-boot-starter-web")

    integrationTestImplementation(projects.boundedContext.place.application)
    integrationTestImplementation(projects.boundedContext.accessibility.application)
    integrationTestImplementation(libs.jackson.module.kotlin)
    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    integrationTestImplementation(libs.mockito.kotlin)
}
