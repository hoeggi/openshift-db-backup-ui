import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    api(compose.desktop.currentOs)
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(projects.settings)
}
