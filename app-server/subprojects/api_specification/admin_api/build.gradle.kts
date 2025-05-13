plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.openapi.generator)
}

dependencies {
    implementation(projects.crossCuttingConcern.stdlib)
}

tasks.getByName("openApiGenerate") {
    inputs.file("${project.rootDir}/../api-admin/api-spec.yaml")
    outputs.dir(layout.buildDirectory.dir("generated-api").get().asFile.path)
    outputs.dir("${project.projectDir.path}/src/main/kotlin")
}

tasks.getByName("compileKotlin") {
    inputs.dir("${project.projectDir.path}/src/main/kotlin")
}

openApiGenerate {
    inputSpec.set("${project.rootDir}/../api-admin/api-spec.yaml")
    packageName.set("club.staircrusher.admin_api.spec")
    apiPackage.set("club.staircrusher.admin_api.spec")
    modelPackage.set("club.staircrusher.admin_api.spec.dto")
    outputDir.set(layout.buildDirectory.dir("generated-api").get().asFile.path)
    generatorName.set("kotlin")
    configOptions.put("sourceFolder", "src/main/kotlin")
    configOptions.put("serializationLibrary", "jackson")
    configOptions.put("enumPropertyNaming", "UPPERCASE")
    skipValidateSpec.set(true)
    generateApiTests.set(false)
    generateModelTests.set(false)
}
val openApiGenerateTask = tasks.getByName("openApiGenerate") {
    doLast {
        copy {
            from(layout.buildDirectory.dir("generated-api/src").get().asFile.path)
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
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
}
