dependencies {
    val awsSdkVersion: String by project
    implementation("software.amazon.awssdk:s3:$awsSdkVersion")
    implementation("software.amazon.awssdk:rekognition:$awsSdkVersion")
    runtimeOnly("software.amazon.awssdk:sts:$awsSdkVersion") // IRSA를 사용하기 위해서 필요함

    implementation("org.springframework:spring-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
}
