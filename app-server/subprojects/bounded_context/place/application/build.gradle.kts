dependencies {
    val kotlinLoggingVersion: String by project
    implementation(projects.apiSpecification.domainEvent)
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    implementation("org.locationtech.jts:jts-core:1.19.0")
}
