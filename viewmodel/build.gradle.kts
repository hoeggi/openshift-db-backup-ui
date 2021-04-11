plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    api(projects.errorhandler)
    api(projects.api.apiModels)
    implementation(projects.api.api)
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.guava)
    compileOnly(Dependencies.slf4j_api)
}
