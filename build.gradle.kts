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
            /**
             * parameter로 받는 source set의 output을 다른 project의 dependency로 사용할 수 있도록 노출해주는 함수.
             * 이 함수를 사용한 source set은 아래 코드 예시처럼 다른 project에서 의존해서 사용할 수 있다.
             *
             * dependencies {
             *     // someBoundedContext의 application source set의 코드에 의존한다.
             *     outputAdapterImplementation(project(":someBoundedContext", "application"))
             * }
             *
             * refs: https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000138910-Gradle-dependency-on-non-main-source-set-module#community_comment_115000181524
             */
            fun exposeArtifact(sourceSet: SourceSet) {
                val jarTask = project.tasks.register("${sourceSet.name}Jar", org.gradle.jvm.tasks.Jar::class) {
                    from(sourceSet.output)
                    group = "build"
                }
                project.configurations {
                    val configurationName = sourceSet.name
                    create(configurationName)
                    project.artifacts.add(configurationName, jarTask)
                }
            }

            val domainSourceSet = create("domain") {
            }

            val applicationSourceSet = create("application") {
                compileClasspath += domainSourceSet.compileClasspath + domainSourceSet.output
                runtimeClasspath += domainSourceSet.runtimeClasspath + domainSourceSet.output

                exposeArtifact(this)
            }

            val outputAdapterSourceSet = create("output-adapter") {
                listOf(domainSourceSet, applicationSourceSet).forEach { sourceSet ->
                    compileClasspath += sourceSet.compileClasspath + sourceSet.output
                    runtimeClasspath += sourceSet.runtimeClasspath + sourceSet.output
                }

                exposeArtifact(this)
            }

            val inputAdapterSourceSet = create("input-adapter") {
                listOf(domainSourceSet, applicationSourceSet).forEach { sourceSet ->
                    compileClasspath += sourceSet.compileClasspath + sourceSet.output
                    runtimeClasspath += sourceSet.runtimeClasspath + sourceSet.output
                }

                exposeArtifact(this)
            }

            val infraSourceSet = create("infra") {
                listOf(domainSourceSet, applicationSourceSet).forEach { sourceSet ->
                    compileClasspath += sourceSet.compileClasspath + sourceSet.output
                    runtimeClasspath += sourceSet.runtimeClasspath + sourceSet.output
                }

                exposeArtifact(this)
            }
        }
    }
}
