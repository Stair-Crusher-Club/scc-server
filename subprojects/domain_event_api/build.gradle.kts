plugins {
    idea
    id("com.squareup.wire")
}

idea {
    module {
        sourceDirs.plusAssign(file("$projectDir/src/main/proto"))
    }
}
sourceSets {
    main {
        java {
            srcDir("build/generated/source/wire")
        }
    }
}


wire {
    kotlin {}
}

dependencies {
    implementation(projects.stdlib)
    implementation(projects.domainEvent)
}
