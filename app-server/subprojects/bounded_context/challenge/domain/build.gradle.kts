dependencies {
    implementation(projects.boundedContext.place.domain)

    implementation("org.hibernate.orm:hibernate-core")
    implementation(libs.jackson.module.kotlin)
}
