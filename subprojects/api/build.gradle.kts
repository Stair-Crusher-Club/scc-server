val kotlinVersion = project.properties["kotlinVersion"] as String

plugins {
    kotlin("jvm")
    id("org.openapi.generator") version "6.0.1"
}

openApiGenerate {
    inputSpec.set("${project.projectDir.path}/scc-api/api-spec.yaml")
    packageName.set("club.staircrusher.api")
    apiPackage.set("club.staircrusher.api")
    modelPackage.set("club.staircrusher.api.dto")
    outputDir.set("${buildDir.path}/generated-api")
    generatorName.set("kotlin")
    configOptions.put("sourceFolder", "src/main/kotlin")
    configOptions.put("serializationLibrary", "jackson")
    skipValidateSpec.set(true)
}
val openApiGenerateTask = tasks.getByName("openApiGenerate") {
    doLast {
        copy {
            from("${buildDir.path}/generated-api/src")
            into("src/")
        }
    }
    finalizedBy("build")
}
tasks.getByName("build").dependsOn(openApiGenerateTask)

// openApiGenerateTask로 생성된 build.gradle.kts의 dependencies를 여기에 복사한다.
dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
}
