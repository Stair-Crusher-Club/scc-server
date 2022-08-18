dependencies {
    domainImplementation(project(":stdlib"))

    infraImplementation("at.favre.lib:bcrypt:0.9.0")
    infraImplementation("com.auth0:java-jwt:3.18.1")
    infraImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")
}
