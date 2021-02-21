private object Versions {
    val kotlin = "1.4.30"
    val okio = "2.10.0"
    val ktor = "1.5.1"
    val kotlinx_coroutines = "1.4.2"
    val kotlinx_serialization = "1.1.0"
    val metrics_jmx = "4.1.17"
    val micrometer_registry_prometheus = "1.6.4"
}

object Dependencies {
    object Kotlin {
        val version = Versions.kotlin
        val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinx_coroutines}"
        val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}"
    }

    object ktor {
        val core = "io.ktor:ktor:${Versions.ktor}"
        val server = "io.ktor:ktor-server-netty:${Versions.ktor}"
        val client = "io.ktor:ktor-client-core:${Versions.ktor}"
        val client_okhttp = "io.ktor:ktor-client-cio:${Versions.ktor}"

        val logging = "io.ktor:ktor-client-logging:${Versions.ktor}"
        val websockets = "io.ktor:ktor-websockets:${Versions.ktor}"
        val serialization = "io.ktor:ktor-serialization:${Versions.ktor}"
        val auth = "io.ktor:ktor-auth:${Versions.ktor}"
        val metrics = "io.ktor:ktor-metrics:${Versions.ktor}"
        val metrics_micrometer = "io.ktor:ktor-metrics-micrometer:${Versions.ktor}"
    }

    val okio = "com.squareup.okio:okio:${Versions.okio}"
    val metrics_jmx = "io.dropwizard.metrics:metrics-jmx:${Versions.metrics_jmx}"
    val micrometer_registry_prometheus =
        "io.micrometer:micrometer-registry-prometheus:${Versions.micrometer_registry_prometheus}"
}