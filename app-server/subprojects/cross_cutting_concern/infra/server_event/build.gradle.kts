dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    api(rootProject.projects.crossCuttingConcern.domain.serverEvent)
    api(rootProject.projects.crossCuttingConcern.application.serverEvent)

    implementation("org.hibernate.orm:hibernate-core")
}
