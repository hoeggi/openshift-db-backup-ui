import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("me.tylerbwong.gradle.metalava")
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

dependencies {
    implementation(compose.desktop.currentOs)
//    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":viewmodel"))
    implementation(project(":i18n"))
    implementation(project(":errorhandler"))
    implementation(project(":settings"))
    implementation(project(":api_models"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.guava)
    implementation(Dependencies.okio)
    implementation(Dependencies.ansi_sequence)
    compileOnly(Dependencies.slf4j_api)
}
