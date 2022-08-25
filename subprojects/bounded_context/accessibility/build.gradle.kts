dependencies {
    domainImplementation(project(":stdlib"))

    outputAdapterImplementation(project(":bounded_context:place", "application"))
}
