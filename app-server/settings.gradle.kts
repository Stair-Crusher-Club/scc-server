rootProject.name = "app-server"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

fileTree("subprojects").filter { it.name == "build.gradle.kts" }.forEach {
    val parentDir = it.parentFile
    val projectName = buildString {
        var dir = parentDir
        while (dir.name != "subprojects") {
            if (dir != parentDir) {
                insert(0, ":")
            }
            insert(0, dir.name)
            dir = dir.parentFile
        }
    }
    include(projectName)
    project(":$projectName").projectDir = parentDir
}


pluginManagement {
    val kotlinVersion: String by settings
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings
    val wireVersion: String by settings
    val kspVersion: String by settings
    val detektVersion: String by settings
    val jibVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.spring") version kotlinVersion apply false
        kotlin("plugin.jpa") version kotlinVersion apply false

        id("org.jetbrains.kotlin.plugin.spring") version kotlinVersion
        id("org.springframework.boot") version springBootVersion
        id("com.squareup.wire") version wireVersion
        id("com.google.devtools.ksp") version kspVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        id("com.google.cloud.tools.jib") version jibVersion

        id("io.spring.dependency-management") version springDependencyManagementVersion
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://repo.spring.io/milestone/")
    }
}
