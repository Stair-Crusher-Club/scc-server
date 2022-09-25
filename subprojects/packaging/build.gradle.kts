plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(project(":admin-api"))
    implementation(project(":stdlib"))
    implementation(project(":bounded_context:place", "domain"))
    implementation(project(":bounded_context:place", "application"))
    implementation(project(":bounded_context:place", "infra"))
    implementation(project(":bounded_context:accessibility", "domain"))
    implementation(project(":bounded_context:accessibility", "application"))
    implementation(project(":bounded_context:accessibility", "infra"))
    implementation(project(":bounded_context:quest", "domain"))
    implementation(project(":bounded_context:quest", "application"))
    implementation(project(":bounded_context:quest", "infra"))

    // TODO: 임시로 넣은 것이므로 삭제 필요
    val kotlinLoggingVersion: String by project
    val kotlinSerializationVersion: String by project
    val coroutineVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutineVersion")
    implementation("io.projectreactor.netty:reactor-netty")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
}
