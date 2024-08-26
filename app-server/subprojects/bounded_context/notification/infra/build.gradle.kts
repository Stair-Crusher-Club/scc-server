dependencies {
    val kotlinLoggingVersion: String by project
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val firebaseAdminVersion: String by project
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava")
    api("com.google.firebase:firebase-admin:$firebaseAdminVersion")
}
