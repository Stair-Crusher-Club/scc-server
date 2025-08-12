dependencies {
    implementation(projects.boundedContext.place.domain)

    implementation("org.hibernate.orm:hibernate-core")
    implementation(libs.jackson.module.kotlin)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
