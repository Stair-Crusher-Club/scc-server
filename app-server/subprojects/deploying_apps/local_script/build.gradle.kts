dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.reactive)
    implementation(libs.kotlin.serialization.json)

    implementation("org.springframework.boot:spring-boot-starter-web")
//    implementation("org.springframework:spring-web:6.0.7")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.place.application)
    implementation(projects.boundedContext.place.infra)
    implementation(projects.boundedContext.accessibility.domain)
}
