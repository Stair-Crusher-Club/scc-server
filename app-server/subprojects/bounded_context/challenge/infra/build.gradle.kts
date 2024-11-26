plugins {
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
}

dependencies {
    api(projects.apiSpecification.api)
    api(projects.apiSpecification.domainEvent)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)

    implementation(libs.aws.sdk.s3)
    runtimeOnly(libs.aws.sdk.sts) // IRSA를 사용하기 위해서 필요함

    testImplementation(projects.boundedContext.place.application)
}
