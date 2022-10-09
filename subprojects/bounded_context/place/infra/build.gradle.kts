plugins {
    idea
    kotlin("plugin.serialization")
    id("io.spring.dependency-management")
    id("org.springframework.boot")
    id("app.cash.sqldelight")
}

// spring boot plugin은 아래의 BOM_COORDINATES를 위해서만 추가한 것이므로, bootJar task는 disable 시켜준다.
tasks.bootJar.get().enabled = false

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

dependencies {
    val kotlinLoggingVersion: String by project
    val kotlinSerializationVersion: String by project
    val sqlDelightVersion: String by project

    implementation(projects.stdlib)

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-webflux")
    implementation("io.projectreactor.netty:reactor-netty")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinSerializationVersion")

    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    // SQL delight
    implementation("app.cash.sqldelight:runtime-jvm:$sqlDelightVersion")
    implementation("app.cash.sqldelight:jdbc-driver:$sqlDelightVersion")
    implementation("app.cash.sqldelight:coroutines-extensions:$sqlDelightVersion")
    implementation("app.cash.sqldelight:postgresql-dialect:$sqlDelightVersion")
}

idea {
    module {
        sourceDirs.plusAssign(file("$projectDir/src/main/sqldelight"))
    }
}

sqldelight {
    val sqlDelightVersion: String by project

    // FIXME: need to determine database name
    database("StairCrusherClub") {
        packageName = "club.staircrusher.place.infra.db"
        dialect("app.cash.sqldelight:postgresql-dialect:$sqlDelightVersion")
        verifyMigrations = false
    }
}

tasks.bootJar { enabled = false }
