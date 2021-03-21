plugins {
    kotlin("jvm")
    id("com.squareup.sqldelight") version Versions.sqldelight
}

sqldelight {
    database("OpenshiftDbGui") {
        schemaOutputDirectory = file("src/main/sqldelight/databases")
        packageName = "io.github.hoeggi.openshiftdb.eventlog"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
    implementation(project(":build-config"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.sqldelight_sqlite)
}
