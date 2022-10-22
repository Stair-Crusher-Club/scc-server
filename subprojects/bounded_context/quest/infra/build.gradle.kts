plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


dependencies {
    implementation(projects.boundedContext.place.application)
    implementation(projects.boundedContext.accessibility.application)
    api(projects.adminApi)

    implementation("org.springframework.boot:spring-boot-starter-web")
}
