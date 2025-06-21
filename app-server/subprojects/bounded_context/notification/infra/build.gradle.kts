plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    api(projects.apiSpecification.api)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation(libs.kotlin.logging)
    implementation(libs.coroutines.guava)
    api(libs.firebase.admin)

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    testImplementation(libs.mockito.kotlin)
}
