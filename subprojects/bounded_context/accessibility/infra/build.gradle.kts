plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":bounded_context:place:application"))

    api(project(":api"))
    implementation(projects.sqldelight)
    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.bootJar { enabled = false }
