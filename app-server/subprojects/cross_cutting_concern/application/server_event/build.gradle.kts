dependencies {
    implementation(rootProject.projects.crossCuttingConcern.stdlib)
    api(rootProject.projects.crossCuttingConcern.domain.serverEvent)

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
