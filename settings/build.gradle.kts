plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
    id("com.github.gmazzo.buildconfig") version "3.0.0"
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"


buildConfig {
    className("BuildConfig")
    useKotlinOutput()
    buildConfigField("String", "APP_NAME", "\"${rootProject.name}\"")
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    implementation(Dependencies.ktor.serialization)
    implementation(Dependencies.okio)
}
