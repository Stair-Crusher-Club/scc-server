dependencies {
    val kotlinLoggingVersion: String by project
    implementation(projects.apiSpecification.domainEvent)
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val jtsVersion: String by project
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.locationtech.jts:jts-core:$jtsVersion")
    implementation("info.debatty:java-string-similarity:2.0.0")
}
