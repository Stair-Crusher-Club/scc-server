plugins {
    idea
    id("app.cash.sqldelight")
}

dependencies {
    implementation(projects.stdlib)

    val sqlDelightVersion: String by project
    api("app.cash.sqldelight:runtime-jvm:$sqlDelightVersion")
    api("app.cash.sqldelight:jdbc-driver:$sqlDelightVersion")
    api("app.cash.sqldelight:coroutines-extensions:$sqlDelightVersion")
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
        packageName = "club.staircrusher.infra.persistence.sqldelight"
        dialect("app.cash.sqldelight:postgresql-dialect:$sqlDelightVersion")
        verifyMigrations = false
    }
}
