plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
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
    implementation(Dependencies.ktor.serialization)
}

tasks.test {
    useJUnit()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    // to allow using kotlin.Result
    kotlinOptions.freeCompilerArgs += "-Xallow-result-return-type"
}
