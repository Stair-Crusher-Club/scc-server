plugins {
    idea
    id("com.squareup.wire")
}

idea {
    module {
        sourceDirs.plusAssign(file("$projectDir/src/main/proto"))
    }
}

wire {
    kotlin {}
}