plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.domain.messageQueue)
    implementation(projects.crossCuttingConcern.application.messageQueue)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-aspects")
    implementation(libs.aws.sdk.sqs)
    runtimeOnly(libs.aws.sdk.sts) // IRSA를 사용하기 위해서 필요함
    implementation(libs.coroutines.jdk8)
}
