import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    api(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib-jdk8"))
}
