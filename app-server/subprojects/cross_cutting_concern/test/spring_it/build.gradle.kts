plugins {
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.spring.boot)
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation(libs.jackson.module.kotlin)
    runtimeOnly("com.zaxxer:HikariCP")
    runtimeOnly(libs.postgresql)

    val dependencyHandlerScope = this
    rootProject.subprojects {
        when (project.name) {
            "domain" -> dependencyHandlerScope.api(this)
            "application" -> dependencyHandlerScope.implementation(this)
            "infra" -> dependencyHandlerScope.runtimeOnly(this)
        }
    }

    implementation(projects.crossCuttingConcern.infra.springWeb)
    implementation(projects.crossCuttingConcern.infra.springMessage)
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
}
