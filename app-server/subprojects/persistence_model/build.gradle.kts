plugins {
    idea
    id("app.cash.sqldelight")
}

dependencies {
    implementation(projects.stdlib)
    implementation(projects.boundedContext.quest.domain)
    implementation(projects.boundedContext.user.domain)

    val sqlDelightVersion: String by project
    api("app.cash.sqldelight:runtime-jvm:$sqlDelightVersion")
    api("app.cash.sqldelight:jdbc-driver:$sqlDelightVersion")
    api("app.cash.sqldelight:coroutines-extensions:$sqlDelightVersion")
    implementation("app.cash.sqldelight:postgresql-dialect:$sqlDelightVersion")

    val kotlinLoggingVersion: String by project
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")

    val flywayVersion: String by project
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    val jacksonModuleKotlinVersion: String by project
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonModuleKotlinVersion")
}

idea {
    module {
        sourceDirs.plusAssign(file("$projectDir/src/main/sqldelight"))
    }
}

sqldelight {
    val sqlDelightVersion: String by project

    // FIXME: need to determine database name
    databases {
        create("scc") {
            packageName.set("club.staircrusher.infra.persistence.sqldelight")
            dialect("app.cash.sqldelight:postgresql-dialect:$sqlDelightVersion")
            verifyMigrations.set(false)
            deriveSchemaFromMigrations.set(true)

            // flyway migration을 위한 설정.
            // sqldelight는 자체적으로 마이그레이션을 실행하지 않는다. (refs: https://github.com/cashapp/sqldelight/issues/1962)
            // 따라서 flyway와 같은 파일을 통해 따로 마이그레이션을 실행해줘야 한다.
            // 아래 옵션을 켜주면 .sqm 파일들을 처리한 후 flyway가 사용할 .sql 마이그레이션 파일로 변경해준다.
            // flyway default는 db/migration 폴더를 스캔하는 것이므로 그 아래에 .sql 파일을 만들어준다.
            migrationOutputDirectory.set(file("$buildDir/resources/main/db/migration"))
            migrationOutputFileFormat.set(".sql")
        }
    }
}
// generateMainStairCrusherClubInterface는 기본적으로 compileKotlin이 의존하는데,
// generateMainStairCrusherClubMigrations은 아닌 듯하다. 그래서 의존성을 손으로 추가해준다.
// refs: https://cashapp.github.io/sqldelight/jvm_postgresql/migrations/
tasks.compileKotlin {
    dependsOn("generateMainsccMigrations")
}
