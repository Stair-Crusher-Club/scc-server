plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    val awsSdkVersion: String by project
    val apacheCommonsVersion: String by project

    api(projects.apiSpecification.api)
    api(projects.apiSpecification.domainEvent)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("io.projectreactor.netty:reactor-netty")
    implementation("org.springframework:spring-webflux")
    implementation("org.apache.commons:commons-csv:$apacheCommonsVersion")
    implementation("software.amazon.awssdk:s3:$awsSdkVersion")
    runtimeOnly("software.amazon.awssdk:sts:$awsSdkVersion") // IRSA를 사용하기 위해서 필요함

    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
    testImplementation(projects.boundedContext.externalAccessibility.application)
}
