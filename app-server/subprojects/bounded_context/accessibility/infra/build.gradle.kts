plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    api(projects.apiSpecification.api)
    implementation(projects.boundedContext.place.domain)
    implementation(projects.boundedContext.challenge.domain)
    implementation(projects.boundedContext.challenge.application)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)

    val awsSdkVersion: String by project
    implementation("software.amazon.awssdk:s3:$awsSdkVersion")
    runtimeOnly("software.amazon.awssdk:sts:$awsSdkVersion") // IRSA를 사용하기 위해서 필요함
    testImplementation(projects.apiSpecification.domainEvent)
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
}
