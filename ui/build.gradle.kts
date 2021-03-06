import org.jetbrains.compose.compose

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
//    id("org.jetbrains.kotlin.kapt")
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
    testImplementation(kotlin("test-junit"))
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":viewmodel"))
    implementation(project(":i18n"))
    api(project(":api_models"))
    implementation(Dependencies.Kotlin.coroutines)
    compileOnly(Dependencies.slf4j_api)
    implementation(Dependencies.guava)
    implementation(Dependencies.okio)
    implementation("net.rubygrapefruit:ansi-control-sequence-util:0.2")
}

tasks.test {
    useJUnit()
}