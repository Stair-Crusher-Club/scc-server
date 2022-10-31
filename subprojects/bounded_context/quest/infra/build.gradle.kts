plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


dependencies {
    implementation(projects.boundedContext.place.application)
    implementation(projects.boundedContext.accessibility.application)
    implementation(projects.persistenceModel)
    api(projects.adminApi)

    val jacksonModuleKotlinVersion: String by project
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Fixme: 다른 모듈에서는 이거 안 해도 테스트 잘만 도는데...?
    val jUnitJupiterVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitJupiterVersion")
}
