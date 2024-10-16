dependencies {
    val kotlinLoggingVersion: String by project
    implementation(projects.apiSpecification.domainEvent)
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val jtsVersion: String by project
    implementation("org.locationtech.jts:jts-core:$jtsVersion")
}
