plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(projects.crossCuttingConcern.stdlib)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.retry:spring-retry")
    implementation("org.springframework:spring-aspects")
    implementation(libs.kotlin.logging)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
