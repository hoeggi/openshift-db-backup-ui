object Versions {
    val kotlin = "1.4.31"
    val okio = "2.10.0"
    val ktor = "1.5.2"
    val kotlinx_coroutines = "1.4.3"
    val kotlinx_serialization = "1.1.0"
    val metrics_jmx = "4.1.18"
    val micrometer_registry_prometheus = "1.6.5"
    val okhttp = "4.9.1"
    val slf4j = "1.7.30"
    val guava = "30.1.1-jre"
    val logback = "1.2.3"
    val compose = "0.4.0-build174"
    val ansi_sequence = "0.2"
    val flatlaf = "1.0"
    val sqldelight = "1.4.4"
    val buildconfig = "3.0.0"
}

object Plugins {
    val compose = "org.jetbrains.compose:compose-gradle-plugin:${Versions.compose}"
    val gradle_versions = "com.github.ben-manes:gradle-versions-plugin:0.38.0"
    val dependency_analysis = "com.autonomousapps:dependency-analysis-gradle-plugin:0.71.0"
    val module_dependency_graph = "com.savvasdalkitsis:module-dependency-graph:0.9"
    val metalava = "me.tylerbwong.gradle:metalava-gradle:0.1.6"
    val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
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
        val logging = "io.ktor:ktor-client-logging:${Versions.ktor}"
        val websockets = "io.ktor:ktor-websockets:${Versions.ktor}"
        val serialization = "io.ktor:ktor-serialization:${Versions.ktor}"
        val auth = "io.ktor:ktor-auth:${Versions.ktor}"
        val metrics = "io.ktor:ktor-metrics:${Versions.ktor}"
        val metrics_micrometer = "io.ktor:ktor-metrics-micrometer:${Versions.ktor}"
    }

    val ansi_sequence = "net.rubygrapefruit:ansi-control-sequence-util:${Versions.ansi_sequence}"
    val logback = "ch.qos.logback:logback-classic:${Versions.logback}"
    val guava = "com.google.guava:guava:${Versions.guava}"
    val slf4j_api = "org.slf4j:slf4j-api:${Versions.slf4j}"
    val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    val okio = "com.squareup.okio:okio:${Versions.okio}"
    val metrics_jmx = "io.dropwizard.metrics:metrics-jmx:${Versions.metrics_jmx}"
    val micrometer_registry_prometheus =
        "io.micrometer:micrometer-registry-prometheus:${Versions.micrometer_registry_prometheus}"
    val flatlaf = "com.formdev:flatlaf:${Versions.flatlaf}"
    val sqldelight_sqlite = "com.squareup.sqldelight:sqlite-driver:${Versions.sqldelight}"
}

object TransitivDependencies {
    private val ktor = arrayOf(
        "io.ktor:ktor-server-host-common",
        "io.ktor:ktor-utils-jvm",
        "io.ktor:ktor-http-cio-jvm",
        "io.ktor:ktor-server-core",
        "io.ktor:ktor-http-jvm"
    )
    private val kotlinx = arrayOf(
        "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm",
        "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm",
        "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm"
    )
    private val compose = arrayOf(
        "org.jetbrains.compose.material:material-icons-extended-desktop",
        "org.jetbrains.compose.ui:ui-desktop",
        "org.jetbrains.compose.ui:ui-unit-desktop",
        "org.jetbrains.compose.ui:ui-geometry-desktop",
        "org.jetbrains.compose.animation:animation-core-desktop",
        "org.jetbrains.compose.animation:animation-desktop",
        "org.jetbrains.compose.ui:ui-graphics-desktop",
        "org.jetbrains.compose.desktop:desktop-jvm",
        "org.jetbrains.compose.material:material-icons-core-desktop",
        "org.jetbrains.compose.ui:ui-text-desktop",
        "org.jetbrains.compose.material:material-desktop",
        "org.jetbrains.compose.foundation:foundation-desktop"
    )
    val dependencies = arrayOf(
        *kotlinx,
        *ktor,
        *compose,
        "io.micrometer:micrometer-core",
        "io.dropwizard.metrics:metrics-core"
    )
}
