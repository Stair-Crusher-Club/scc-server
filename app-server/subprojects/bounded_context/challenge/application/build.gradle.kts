dependencies {
    implementation(projects.boundedContext.user.application)
    implementation(projects.boundedContext.place.application)
    implementation(projects.apiSpecification.domainEvent)

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
}
