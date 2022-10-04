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
        maven(url = "https://repo.osgeo.org/repository/release/") // for org.geotools
        mavenCentral()
        maven(url = "https://repo.spring.io/milestone/")
    }

    dependencies {
        val jUnitJupiterVersion: String by project
        testImplementation("org.junit.jupiter:junit-jupiter:$jUnitJupiterVersion")

        if (project.name == "infra") {
            implementation(project(":spring-web"))
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
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
                implementation(applicationProject)
            }
        }

        val springContextVersion: String by project
        val coroutineVersion: String by project
        listOfNotNull(domainProject, applicationProject, infraProject)
            .forEach {
                it.dependencies {
                    implementation(project(":stdlib"))
                    implementation("org.springframework:spring-context:$springContextVersion") // TODO: custom @Component가 동작하지 않아서 임시로 Spring의 @Component를 사용하기 위함임
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
            runtimeOnly(project)
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
