plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":bounded_context:place:application"))

    api(project(":api"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.bootJar { enabled = false }
