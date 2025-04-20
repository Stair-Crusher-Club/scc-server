dependencies {
    api(projects.boundedContext.user.application)
    implementation(projects.boundedContext.challenge.application)
    implementation(projects.boundedContext.misc.application)
    implementation(projects.apiSpecification.domainEvent)

    implementation(libs.thumbnailator)
    implementation(libs.webp.imageio)
    implementation(libs.kotlin.logging)
    implementation(libs.jts.core)
    implementation(libs.guava)
    implementation(libs.java.cv)
    implementation(variantOf(libs.open.cv) { classifier("linux-x86_64") })
    implementation(variantOf(libs.java.cpp) { classifier("linux-x86_64") })
    implementation(variantOf(libs.open.blas) { classifier("linux-x86_64") })
}
