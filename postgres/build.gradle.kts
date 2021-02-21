plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    api(project(":common"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.ktor.auth)
    implementation(Dependencies.Kotlin.serialization)
    implementation(Dependencies.okio)
}

tasks.test {
    useJUnit()
}