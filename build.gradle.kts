import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
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
        maven(url = "https://repo.spring.io/milestone/")
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
        /**
         * 아래와 같이 source set hierarchy를 구축한다.
         *
         * domain <- application <- infra
         */
        sourceSets {
            /**
             * parameter로 받는 source set의 output을 다른 project의 dependency로 사용할 수 있도록 노출해주는 함수.
             * 이 함수를 사용한 source set은 아래 코드 예시처럼 다른 project에서 의존해서 사용할 수 있다.
             *
             * dependencies {
             *     // someBoundedContext의 application source set의 코드에 의존한다.
             *     infraImplementation(project(":someBoundedContext", "application"))
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
                // domain source set은 외부에서 의존하지 못하도록 exposeArtifact를 하지 않는다.
            }

            val applicationSourceSet = create("application") {
                compileClasspath += domainSourceSet.output
                runtimeClasspath += domainSourceSet.output

                exposeArtifact(this)
            }

            val infraSourceSet = create("infra") {
                listOf(domainSourceSet, applicationSourceSet).forEach { sourceSet ->
                    compileClasspath += sourceSet.output
                    runtimeClasspath += sourceSet.output
                }

                exposeArtifact(this)
            }
        }

        fun addCommonDependenciesToAllSourceSets(project: Project) {
            project.sourceSets.forEach { sourceSet ->
                project.dependencies {
                    add(sourceSet.implementationConfigurationName, project(":stdlib"))
                }
            }
        }

        addCommonDependenciesToAllSourceSets(project)
    }
}
