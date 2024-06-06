dependencies {
    api(projects.boundedContext.user.application)
    implementation(projects.boundedContext.place.application)
    implementation(projects.boundedContext.challenge.application)

    implementation(projects.apiSpecification.domainEvent)

    implementation("net.coobird:thumbnailator:0.4.20")
}
