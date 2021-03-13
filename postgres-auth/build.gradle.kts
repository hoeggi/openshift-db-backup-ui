plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.Kotlin.coroutines)
    api(Dependencies.ktor.auth)
    implementation(Dependencies.Kotlin.serialization)
    compileOnly(Dependencies.slf4j_api)
}
