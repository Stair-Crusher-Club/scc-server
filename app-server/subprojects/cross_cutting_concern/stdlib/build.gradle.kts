plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    val geoToolsVersion: String by project
    val kopyKatVersion: String by project
    implementation("org.geotools:gt-referencing:$geoToolsVersion")
    implementation("com.auth0:java-jwt:3.18.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")

    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.data:spring-data-commons")

    ksp("at.kopyk:kopykat-ksp:$kopyKatVersion")
    compileOnly("at.kopyk:kopykat-annotations:$kopyKatVersion")

    val jUnitJupiterVersion: String by project
    testImplementation("org.junit.jupiter:junit-jupiter-api:$jUnitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$jUnitJupiterVersion")
}

ksp {
    arg("generate", "annotated")
}
