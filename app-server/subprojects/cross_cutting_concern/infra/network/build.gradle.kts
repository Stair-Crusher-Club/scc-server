dependencies {
    implementation(projects.crossCuttingConcern.stdlib)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation(libs.kotlin.serialization.json)
    implementation(libs.bucket4j.core)
}
