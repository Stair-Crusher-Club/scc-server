dependencies {
    implementation(libs.kotlin.serialization.json)
    implementation(libs.jackson.module.kotlin)
    implementation("org.hibernate.orm:hibernate-core")

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
