plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
//    id("com.github.gmazzo.buildconfig") version "3.0.0"
}

//buildConfig {
//    className("BuildConfig")
//    useKotlinOutput()
//    buildConfigField("String", "APP_NAME", "\"${rootProject.name}\"")
//}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    implementation(project(":build-config"))
    implementation(Dependencies.Kotlin.serialization)
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.okio)
}
