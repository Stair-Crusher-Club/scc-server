plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spring.boot)
}

dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    implementation(projects.crossCuttingConcern.infra.network)
    implementation(projects.apiSpecification.domainEvent)
    implementation(projects.boundedContext.misc.application)
    implementation(projects.boundedContext.challenge.domain)
    implementation(projects.boundedContext.challenge.application)
    implementation(projects.boundedContext.challenge.infra)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation(libs.coroutines.jdk8)
    implementation(libs.coroutines.reactive)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.logging)
    implementation(libs.guava)
    implementation(libs.aws.sdk.s3)
    implementation(libs.aws.sdk.rekognition)
    runtimeOnly(libs.aws.sdk.sts) // IRSA를 사용하기 위해서 필요함
    implementation(libs.thumbnailator)
    implementation(libs.webp.imageio)
    implementation(libs.google.genai)

    implementation("com.openai:openai-java:1.5.0")
    implementation(libs.open.cv)
    implementation(libs.java.cpp)
    implementation(libs.open.blas)
    implementation(libs.java.cv)

    integrationTestImplementation(projects.apiSpecification.domainEvent)
    integrationTestImplementation(libs.mockito.kotlin)
    integrationTestImplementation(libs.jackson.module.kotlin)
    integrationTestImplementation(projects.crossCuttingConcern.test.springIt)
}
