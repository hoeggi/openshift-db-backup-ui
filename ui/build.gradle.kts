import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
//    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    jcenter()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(compose.animation)
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":viewmodel"))
    implementation(project(":i18n"))
    implementation(project(":errorhandler"))
    implementation(project(":settings"))
    implementation(Dependencies.Kotlin.coroutines)
    compileOnly(Dependencies.slf4j_api)
    implementation(Dependencies.guava)
    implementation(Dependencies.okio)
    implementation(Dependencies.ansi_sequence)
}
