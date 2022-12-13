plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://repo.spring.io/milestone/")
}

dependencies {
    val kotlinVersion: String by project
    val springBootVersion: String by project
    val springDependencyManagementVersion: String by project
    val detektVersion: String by project
    val testLoggerVersion: String by project
    val kspVersion: String by project

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("org.springframework.boot:spring-boot-gradle-plugin:$springBootVersion")
    implementation("io.spring.gradle:dependency-management-plugin:$springDependencyManagementVersion")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:$detektVersion")
    implementation("com.adarshr:gradle-test-logger-plugin:$testLoggerVersion")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:$kspVersion")
}
