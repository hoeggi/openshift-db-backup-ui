import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    api(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(projects.ui.navigation)
    implementation(projects.ui.viewmodelExtensions)

    compileOnly(Dependencies.slf4j_api)
}
