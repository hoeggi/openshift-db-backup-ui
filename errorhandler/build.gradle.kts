plugins {
    kotlin("jvm")
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.Kotlin.coroutines)
    compileOnly(Dependencies.slf4j_api)
}