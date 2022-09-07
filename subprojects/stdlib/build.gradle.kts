dependencies {
    val jakartaInjectVersion: String by project
    implementation("jakarta.inject:jakarta.inject-api:$jakartaInjectVersion")

    implementation("org.geotools:gt-referencing:26.5")
}
