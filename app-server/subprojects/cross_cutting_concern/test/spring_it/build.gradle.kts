plugins {
    id("io.spring.dependency-management")
    id("org.springframework.boot")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    runtimeOnly("com.zaxxer:HikariCP")
    val postgresqlVersion: String by project
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("org.springframework:spring-jdbc")

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
