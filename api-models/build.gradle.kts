plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
    implementation(Dependencies.Kotlin.serialization)
}
