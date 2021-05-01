plugins {
    kotlin("jvm")
    id("com.squareup.sqldelight") version Versions.sqldelight
}

sqldelight {
    database("SyslogDb") {
        packageName = "io.github.hoeggi.openshiftdb.syslog"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.sqldelight_sqlite)
    implementation(Dependencies.sqldelight_coroutines)
    implementation(Dependencies.logback)
    implementation(Dependencies.sqlite)
}
