dependencies {
    api(projects.boundedContext.user.application)
    implementation(projects.boundedContext.challenge.application)
    implementation(projects.boundedContext.misc.application)
    implementation(projects.apiSpecification.domainEvent)

    implementation(libs.thumbnailator)
    implementation(libs.webp.imageio)
    implementation(libs.java.cv)
    implementation(libs.kotlin.logging)
    implementation(libs.jts.core)
    implementation(libs.guava)
}
