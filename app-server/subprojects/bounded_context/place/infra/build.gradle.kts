plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation(projects.crossCuttingConcern.infra.network)
    implementation(projects.apiSpecification.domainEvent)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation(libs.coroutines.reactive)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.logging)
    implementation(libs.guava)

    testImplementation(projects.apiSpecification.domainEvent)
    testImplementation(libs.mockito.kotlin)
    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
}
