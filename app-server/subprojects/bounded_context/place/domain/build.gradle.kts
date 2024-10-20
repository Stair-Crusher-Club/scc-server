dependencies {
    val jtsVersion: String by project
    implementation("org.hibernate:hibernate-spatial:6.1.7.Final")
    implementation("org.locationtech.jts:jts-core:$jtsVersion")
}
