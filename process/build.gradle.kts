plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    api(Dependencies.okio)
    compileOnly(Dependencies.slf4j_api)
}
