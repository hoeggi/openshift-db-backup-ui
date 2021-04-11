import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib-jdk8"))
    implementation(projects.viewmodel)
    implementation(projects.i18n)
    implementation(projects.errorhandler)
    implementation(projects.settings)
    implementation(projects.api.apiModels)
    implementation(Dependencies.Kotlin.coroutines)
    compileOnly(Dependencies.slf4j_api)
}
