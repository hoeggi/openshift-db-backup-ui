plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    implementation(project(":process"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.ktor.auth)
    implementation(Dependencies.Kotlin.serialization)
    implementation(Dependencies.okio)
}
