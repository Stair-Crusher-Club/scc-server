dependencies {
    implementation(libs.aws.sdk.s3)
    implementation(libs.aws.sdk.rekognition)
    runtimeOnly(libs.aws.sdk.sts) // IRSA를 사용하기 위해서 필요함
    implementation("org.springframework:spring-webflux")
    implementation(libs.coroutines.jdk8)

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
}
