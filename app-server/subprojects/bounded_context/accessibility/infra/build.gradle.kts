plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    api(project(":api"))
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.challenge.domain)
    implementation(projects.boundedContext.challenge.application)
    implementation(projects.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")

    integrationTestImplementation(projects.testing.springIt)

    val awsSdkVersion: String by project
    implementation("software.amazon.awssdk:s3:$awsSdkVersion")
    runtimeOnly("software.amazon.awssdk:sts:$awsSdkVersion") // IRSA를 사용하기 위해서 필요함
    testImplementation(projects.domainEvent)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}
