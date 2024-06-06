dependencies {
    implementation(projects.boundedContext.accessibility.application)
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.place.application)

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-web")
}
