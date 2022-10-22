plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":stdlib"))
    implementation(project(":bounded_context:user:application"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-security")

    val jacksonModuleKotlinVersion: String by project
    integrationTestImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
}
