val coroutineVersion: String by project
val kotlinxSerializationVersion: String by project

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")

    implementation("org.springframework.boot:spring-boot-starter-web:3.0.5")
//    implementation("org.springframework:spring-web:6.0.7")
    implementation("org.springframework:spring-webflux:6.0.7")
    implementation("io.projectreactor.netty:reactor-netty:1.1.5")

    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.place.application)
    implementation(projects.boundedContext.place.infra)
    implementation(projects.boundedContext.accessibility.domain)
}
