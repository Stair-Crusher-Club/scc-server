plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.geotools.referencing)
    implementation(libs.jts.core)
    implementation(libs.proj4j.core)
    implementation(libs.proj4j.epsg)
    implementation(libs.java.jwt)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.coroutines.core)
    implementation(libs.apache.commons.text)

    api("jakarta.persistence:jakarta.persistence-api")
    api("org.springframework.data:spring-data-commons")
    implementation("org.hibernate.orm:hibernate-core")

    ksp(libs.kopykat.ksp)
    compileOnly(libs.kopykat.annotations)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

ksp {
    arg("generate", "annotated")
}
