plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    implementation(Dependencies.Kotlin.serialization)
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.okhttp)
    implementation(Dependencies.okio)

    api(project(":api-models"))
    implementation(project(":routes"))
    compileOnly(Dependencies.slf4j_api)
}
