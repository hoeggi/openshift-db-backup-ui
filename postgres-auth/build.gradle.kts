plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(Dependencies.Kotlin.coroutines)
    api(Dependencies.ktor.auth)
    implementation(Dependencies.Kotlin.serialization)
    compileOnly(Dependencies.slf4j_api)
}
