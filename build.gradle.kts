import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.21"
}

repositories {
    mavenCentral()
}

group = "club.staircrusher"
version = "1.0.0-SNAPSHOT"

subprojects {
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    if (project.path.startsWith(":bounded_context:")) {
        sourceSets {
            val domainSourceSet = create("domain") {
            }

            val applicationSourceSet = create("application") {
                compileClasspath += domainSourceSet.compileClasspath
                runtimeClasspath += domainSourceSet.runtimeClasspath
            }

            val outputAdapterSourceSet = create("output-adapter") {
                listOf(domainSourceSet, applicationSourceSet).forEach { sourceSet ->
                    compileClasspath += sourceSet.compileClasspath
                    runtimeClasspath += sourceSet.runtimeClasspath
                }
            }

            val inputAdapterSourceSet = create("input-adapter") {
                listOf(domainSourceSet, applicationSourceSet).forEach { sourceSet ->
                    compileClasspath += sourceSet.compileClasspath
                    runtimeClasspath += sourceSet.runtimeClasspath
                }
            }
        }
    }
}
