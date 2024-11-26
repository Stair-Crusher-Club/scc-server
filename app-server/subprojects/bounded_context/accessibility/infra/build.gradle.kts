plugins {
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
}

dependencies {
    api(projects.apiSpecification.api)
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.challenge.domain)
    implementation(projects.boundedContext.challenge.application)
    implementation(projects.boundedContext.challenge.infra)
    implementation(projects.boundedContext.misc.application)
    implementation(projects.crossCuttingConcern.infra.network)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation(libs.coroutines.jdk8)

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)

    implementation(libs.aws.sdk.s3)
    implementation(libs.aws.sdk.rekognition)
    runtimeOnly(libs.aws.sdk.sts) // IRSA를 사용하기 위해서 필요함
    testImplementation(projects.apiSpecification.domainEvent)
    testImplementation(libs.mockito.kotlin)
}
