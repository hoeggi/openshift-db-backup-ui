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
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    implementation(projects.buildConfig)
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.sqldelight_sqlite)
    implementation(Dependencies.logback)
}
