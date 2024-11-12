dependencies {
    api(projects.boundedContext.user.application)
    implementation(projects.boundedContext.place.application)
    implementation(projects.boundedContext.challenge.application)
    implementation(projects.boundedContext.misc.application)

    implementation(projects.apiSpecification.domainEvent)

    implementation("net.coobird:thumbnailator:0.4.20")
    implementation("org.sejda.webp-imageio:webp-imageio-sejda:0.1.0")
    implementation("org.bytedeco:javacv-platform:1.5.9")

    testImplementation(libs.mockito.kotlin)

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-web")
}
