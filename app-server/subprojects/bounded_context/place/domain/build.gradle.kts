dependencies {
    implementation("org.hibernate:hibernate-spatial:6.1.7.Final")
    implementation(libs.jts.core)
    implementation(libs.jackson.module.kotlin)

    implementation(projects.boundedContext.user.domain)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
