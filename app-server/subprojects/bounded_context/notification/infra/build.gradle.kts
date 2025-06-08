dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation(libs.kotlin.logging)
    implementation(libs.coroutines.guava)
    api(libs.firebase.admin)
}
