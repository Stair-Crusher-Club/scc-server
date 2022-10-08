plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// spring boot plugin은 Controller 구현을 위해서만 추가한 것이므로, bootJar task는 disable 시켜준다.
tasks.bootJar.get().enabled = false

dependencies {
    implementation(project(":bounded_context:place:application"))
    implementation(project(":bounded_context:accessibility:application"))
    api(project(":admin-api"))

    implementation("org.springframework.boot:spring-boot-starter-web")
}

tasks.bootJar { enabled = false }
