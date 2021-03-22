plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    api(project(":errorhandler"))
    implementation(project(":api"))
    api(project(":api-models"))
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.guava)
    compileOnly(Dependencies.slf4j_api)
}
