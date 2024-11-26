plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openapi.generator)
}

dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
}

tasks.getByName("openApiGenerate") {
    inputs.file("${project.projectDir.path}/scc-api/api-spec.yaml")
    outputs.dir("${buildDir.path}/generated-api")
    outputs.dir("${project.projectDir.path}/src/main/kotlin")
}

tasks.getByName("compileKotlin") {
    inputs.dir("${project.projectDir.path}/src/main/kotlin")
}

openApiGenerate {
    inputSpec.set("${project.projectDir.path}/scc-api/api-spec.yaml")
    packageName.set("club.staircrusher.api.spec")
    apiPackage.set("club.staircrusher.api.spec")
    modelPackage.set("club.staircrusher.api.spec.dto")
    outputDir.set("${buildDir.path}/generated-api")
    generatorName.set("kotlin")
    configOptions.put("sourceFolder", "src/main/kotlin")
    configOptions.put("serializationLibrary", "jackson")
    configOptions.put("enumPropertyNaming", "UPPERCASE")
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
tasks.getByName("compileKotlin").dependsOn(openApiGenerateTask)

// openApiGenerateTask로 생성된 build.gradle.kts의 dependencies를 여기에 복사한다.
dependencies {
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.3")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
}
