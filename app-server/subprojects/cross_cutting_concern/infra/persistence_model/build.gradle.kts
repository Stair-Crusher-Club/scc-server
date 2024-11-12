plugins {
    idea
}

dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.boundedContext.accessibility.domain)
    implementation(projects.boundedContext.challenge.domain)
    implementation(projects.boundedContext.quest.domain)
    implementation(projects.boundedContext.user.domain)
    implementation(projects.boundedContext.externalAccessibility.domain)
    implementation(projects.crossCuttingConcern.domain.serverEvent)

    implementation(libs.kotlin.logging)
    implementation(libs.flyway.core)
    implementation(libs.jackson.module.kotlin)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
}
