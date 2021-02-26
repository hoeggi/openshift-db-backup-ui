plugins {
    kotlin("jvm")
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    implementation(Dependencies.okio)
    compileOnly(Dependencies.slf4j_api)
}

tasks.test {
    useJUnit()
}