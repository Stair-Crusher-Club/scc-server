dependencies {
    api(projects.boundedContext.user.application)
    implementation(projects.boundedContext.challenge.application)
    implementation(projects.boundedContext.misc.application)
    implementation(projects.apiSpecification.domainEvent)

    implementation(libs.kotlin.logging)
    implementation(libs.jts.core)
    implementation(libs.guava)
    implementation(libs.coroutines.jdk8)
}
