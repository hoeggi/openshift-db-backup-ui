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
    implementation(project(":oc"))
    implementation(project(":api"))
    implementation(project(":postgres"))

    implementation(Dependencies.metrics_jmx)
    implementation(Dependencies.micrometer_registry_prometheus)
    implementation(Dependencies.okio)

    implementation(Dependencies.ktor.core)
    implementation(Dependencies.ktor.server)
    implementation(Dependencies.ktor.logging)
    implementation(Dependencies.ktor.websockets)
    implementation(Dependencies.ktor.auth)
    implementation(Dependencies.ktor.serialization)
    implementation(Dependencies.ktor.metrics)
    implementation(Dependencies.ktor.metrics_micrometer)
}
