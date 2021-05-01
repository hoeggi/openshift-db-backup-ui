import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(projects.viewmodel.viewmodelImpl)
    implementation(projects.i18n)
    implementation(projects.errorhandler)
    implementation(projects.settings)
    implementation(projects.api.apiModels)
    implementation(projects.ui.filechooser)
    implementation(projects.ui.theme)
    implementation(projects.ui.baseElements)
    implementation(projects.ui.navigation)
    implementation(projects.ui.eventlog)
    implementation(projects.ui.viewmodelExtensions)

    compileOnly(Dependencies.slf4j_api)
}
