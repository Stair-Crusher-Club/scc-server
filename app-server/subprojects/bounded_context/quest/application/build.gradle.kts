dependencies {
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.place.application)

    implementation(libs.coroutines.core)

    testImplementation(libs.mockito.kotlin)

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-web")
}
