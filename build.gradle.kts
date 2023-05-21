import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    id("org.springframework.boot")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
}

tasks.bootJar { enabled = false }

group = "club.staircrusher"
version = "1.0.0-SNAPSHOT"

val generatedProject = listOf(project(":admin_api"), project("api"))
subprojects {
    apply(plugin = "kotlin")

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        maven(url = "https://repo.osgeo.org/repository/release/") // for org.geotools
        maven(url = "https://repo.spring.io/milestone/")
        mavenCentral()
    }

    dependencies {
        if (project.name == "infra") {
            implementation(project(":spring_web"))
        }
    }

    tasks.test {
        useJUnitPlatform {
            includeEngines("junit-jupiter")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    tasks.withType<BootJar>() {
        enabled = false
    }

    if (this !in generatedProject) {
        apply(plugin = "io.gitlab.arturbosch.detekt")

        detekt {
            config = files("$rootDir/detekt-config.yml")
            source = objects.fileCollection().from(
                io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_SRC_DIR_KOTLIN,
                io.gitlab.arturbosch.detekt.extensions.DetektExtension.DEFAULT_TEST_SRC_DIR_KOTLIN,
            )
            autoCorrect = true
            buildUponDefaultConfig = true
        }

        tasks.withType<Detekt>().configureEach {
            reports {
                html.required.set(true)
                md.required.set(true)
            }
        }
    }
}

/**
 * bounded context project의 일반적인 dependency 설정.
 * 1. 기본적으로 아래와 같이 domain, application, infra 간의 dependency를 추가한다.
 *
 *            domain <---(api)--- application <---(api)--- infra
 *
 *    - application -> domain: application의 유즈 케이스 함수들이 domain model을 반환할 수 있으므로 api configuration을 사용한다.
 *    - infra -> application: domain과 application에서 정의한 output port interface를 구현해야 할 수 있으므로 api configuration을 사용한다.
 *
 * 2. 모든 bounded context project에 공통으로 들어가야 하는 의존성을 추가한다.
 *
 * 이를 위의 subprojects {} 루프와 다른 루프에서 하는 이유는, kotlin plugin이 적용된 project에만 api configuration이 존재하기 때문이다.
 * 모든 project에 api configuration이 추가되기 전에 아래 루프가 돌면 api configuration을 찾을 수 없다고 에러가 난다.
 * (루프 내에서 :bounded_context:xxx의 순서가 :bounded_context:xxx:(domain|application|infra)보다 빠르기 때문임)
 */
val boundedContextRootProjectPathRegex = Regex("^:bounded_context:([^:]+)\$")
subprojects {
    if (boundedContextRootProjectPathRegex.find(project.path) != null) {
        /**
         * 아래와 같이 source set hierarchy를 구축한다.
         *
         *
         */
        val domainProject = project.childProjects["domain"]
        val applicationProject = project.childProjects["application"]
        val infraProject = project.childProjects["infra"]
        if (domainProject != null && applicationProject != null) {
            applicationProject.dependencies {
                api(domainProject) // 일반적으로 유즈 케이스 메소드의 반환값이 도메인 모델일 수 있다.
            }
        }
        if (applicationProject != null && infraProject != null) {
            infraProject.dependencies {
                api(applicationProject)
            }
        }

        val coroutineVersion: String by project
        listOfNotNull(domainProject, applicationProject, infraProject)
            .forEach {
                it.dependencies {
                    implementation(project(":stdlib"))
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
                }
            }
    }
}


/**
 * 모든 bounded context를 모아서 modular monolith artifact(= bootJar)를 만들기 위한 설정.
 * 1. packaging project에 모든 infra project를 dependency로 추가해준다.
 * 2. 각 bounded context의 domain/application/infra project의 output jar 이름을 customize 해준다.
 *    기본 이름은 전부 <layer 이름>.jar인데, 이러면 packaging에서 bootJar를 만들 때 jar 이름이 충돌해서 에러가 난다.
 *    따라서 각 project의 jar 이름이 모두 다르도록 설정해준다.
 */
val packagingProject = project.childProjects["packaging"]!!
val layerProjectPathRegex = Regex("^:bounded_context:([^:]+):(domain|application|infra)\$")
subprojects {
    if (project.path.endsWith(":infra")) {
        packagingProject.dependencies {
            implementation(project)
        }
    }

    val regexMatch = layerProjectPathRegex.find(project.path)
    if (regexMatch != null) {
        val boundedContextName = regexMatch.groups[1]!!.value
        val layerName = regexMatch.groups[2]!!.value
        project.tasks.jar.get().apply {
            archiveBaseName.set(boundedContextName)
            archiveAppendix.set(layerName)
        }
    }
}

/**
 * 테스트 계층화 세팅.
 * 모든 gradle subprojects의 테스트를 unitTest와 integrationTest로 나눈다.
 * unitTest에서는 @SpringBootTest를 사용하지 않도록 강제한다.
 */
val testLayerNames = listOf("unitTest", "integrationTest") // 낮은 위계에서 높은 위계 순으로 작성되어야 함.
testLayerNames.forEachIndexed { idx, testLayerName ->
    val rootProjectTestTask = tasks.register<Task>(testLayerName)
    tasks.test {
        dependsOn(rootProjectTestTask)
    }

    subprojects {
        sourceSets.create(testLayerName) {
            /**
             * main과 test, 그리고 하위 테스트 계층 source set의 컴파일 결과가 compileClasspath / runtimeClasspath에 추가되도록 한다.
             */
            val mainSourceSet = sourceSets["main"]
            val testSourceSet = sourceSets["test"]
            compileClasspath += mainSourceSet.output + testSourceSet.output
            runtimeClasspath += mainSourceSet.output + testSourceSet.output

            testLayerNames.take(idx).forEach { lowerLayerTestSourceSetName ->
                val lowerLayerTestSourceSet = sourceSets[lowerLayerTestSourceSetName]
                compileClasspath += lowerLayerTestSourceSet.output
                runtimeClasspath += lowerLayerTestSourceSet.output
            }

            /**
             * main과 test source set의 의존성에 같이 의존하게 한다.
             */
            configurations {
                getByName("${testLayerName}Implementation") {
                    extendsFrom(configurations["implementation"])
                    extendsFrom(configurations["testImplementation"])
                }

                getByName("${testLayerName}RuntimeOnly") {
                    extendsFrom(configurations["runtimeOnly"])
                    extendsFrom(configurations["testRuntimeOnly"])
                }
            }
        }

        val subprojectTestTask = tasks.register<Test>(testLayerName) {
            val testSourceSet = sourceSets[testLayerName]
            testClassesDirs = testSourceSet.output.classesDirs
            classpath = testSourceSet.runtimeClasspath

            useJUnitPlatform {
                includeEngines("junit-jupiter")
            }
        }
        rootProjectTestTask {
            dependsOn(subprojectTestTask)
        }
        tasks.test {
            dependsOn(subprojectTestTask)
        }
    }
}
