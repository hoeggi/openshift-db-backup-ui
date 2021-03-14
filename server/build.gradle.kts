plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version Dependencies.Kotlin.version
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test-junit"))
    implementation(project(":oc"))
    implementation(project(":api_models"))
    implementation(project(":postgres"))
    implementation(project(":postgres-auth"))


    implementation(Dependencies.metrics_jmx)
    implementation(Dependencies.micrometer_registry_prometheus)
    implementation(Dependencies.ktor.core)
    implementation(Dependencies.ktor.server)
    implementation(Dependencies.ktor.logging)
    implementation(Dependencies.ktor.websockets)
    implementation(Dependencies.ktor.auth)
    implementation(Dependencies.ktor.serialization)
    implementation(Dependencies.ktor.metrics)
    implementation(Dependencies.ktor.metrics_micrometer)

    compileOnly(Dependencies.slf4j_api)
}
