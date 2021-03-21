import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib"))
    implementation(project(":viewmodel"))
    implementation(project(":i18n"))
    implementation(project(":errorhandler"))
    implementation(project(":settings"))
    implementation(project(":api-models"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.guava)
    implementation(Dependencies.okio)
    implementation(Dependencies.ansi_sequence)
    compileOnly(Dependencies.slf4j_api)
}
