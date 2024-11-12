plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}


dependencies {
    implementation(projects.boundedContext.place.application)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation(projects.crossCuttingConcern.infra.network)
    api(projects.apiSpecification.adminApi)
    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")

    // Fixme: 다른 모듈에서는 이거 안 해도 테스트 잘만 도는데...?
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
