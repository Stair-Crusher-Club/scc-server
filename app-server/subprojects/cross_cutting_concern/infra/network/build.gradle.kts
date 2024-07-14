dependencies {
    val kotlinxSerializationVersion: String by project

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
}
