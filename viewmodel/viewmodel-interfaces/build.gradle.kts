plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(projects.api.apiModels)
    implementation(Dependencies.Kotlin.coroutines)
}
