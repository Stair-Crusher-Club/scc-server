dependencies {
    infraImplementation(project(":bounded_context:place", "domain")) // TODO: 삭제
    infraImplementation(project(":bounded_context:place", "application"))
}
