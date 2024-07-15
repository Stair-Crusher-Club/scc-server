dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
    implementation(projects.crossCuttingConcern.infra.persistenceModel)
    api(rootProject.projects.crossCuttingConcern.domain.serverLog)
    api(rootProject.projects.crossCuttingConcern.application.serverLog)
}
