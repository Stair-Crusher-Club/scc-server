plugins {
    id("com.google.devtools.ksp")
}

dependencies {
    val geoToolsVersion: String by project
    val kopyKatVersion: String by project
    implementation("org.geotools:gt-referencing:$geoToolsVersion")

    ksp("at.kopyk:kopykat-ksp:$kopyKatVersion")
    compileOnly("at.kopyk:kopykat-annotations:$kopyKatVersion")
}

ksp {
    arg("generate", "annotated")
}