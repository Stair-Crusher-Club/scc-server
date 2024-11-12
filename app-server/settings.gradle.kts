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
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven(url = "https://repo.spring.io/milestone/")
    }
}
