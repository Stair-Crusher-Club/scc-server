plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.spring)
}

dependencies {
    api(projects.apiSpecification.api)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation(projects.crossCuttingConcern.application.serverEvent)
    implementation(projects.crossCuttingConcern.infra.network)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation(libs.bcrypt)
    implementation(libs.java.jwt)
    implementation(libs.coroutines.reactive)
    implementation(libs.kotlin.serialization.json)

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    testImplementation(libs.mockito.kotlin)
}
