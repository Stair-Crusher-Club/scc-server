dependencies {
    val jakartaInjectVersion: String by project
    implementation("jakarta.inject:jakarta.inject-api:$jakartaInjectVersion")

    val geoToolsVersion: String by project
    implementation("org.geotools:gt-referencing:$geoToolsVersion")

    val springContextVersion: String by project
    implementation("org.springframework:spring-context:$springContextVersion")
}
