plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(kotlin("test-junit"))
    api(Dependencies.okio)
    compileOnly(Dependencies.slf4j_api)
}
