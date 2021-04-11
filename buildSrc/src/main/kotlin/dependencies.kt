object Versions {
    const val kotlin = "1.4.32"
    const val okio = "2.10.0"
    const val ktor = "1.5.3"
    const val kotlinx_coroutines = "1.4.3"
    const val kotlinx_serialization = "1.1.0"
    const val metrics_jmx = "4.1.19"
    const val micrometer_registry_prometheus = "1.6.5"
    const val okhttp = "4.9.1"
    const val slf4j = "1.7.30"
    const val guava = "30.1.1-jre"
    const val logback = "1.2.3"
    const val compose = "0.4.0-build180"
    const val flatlaf = "1.1.2"
    const val sqldelight = "1.4.4"
    const val buildconfig = "3.0.0"
    const val sqlite = "3.34.0"
}

object Plugins {
    const val compose = "org.jetbrains.compose:compose-gradle-plugin:${Versions.compose}"
    const val gradle_versions = "com.github.ben-manes:gradle-versions-plugin:0.38.0"
    const val dependency_analysis = "com.autonomousapps:dependency-analysis-gradle-plugin:0.71.0"
    const val module_dependency_graph = "com.savvasdalkitsis:module-dependency-graph:0.9"
//    const val metalava = "me.tylerbwong.gradle:metalava-gradle:0.1.6"
    const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
    const val spotless = "com.diffplug.spotless:spotless-plugin-gradle:5.12.0"
}

object Dependencies {
    object Kotlin {
        const val version = Versions.kotlin
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinx_coroutines}"
        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.kotlinx_serialization}"
        const val serialization_cbor = "org.jetbrains.kotlinx:kotlinx-serialization-cbor:${Versions.kotlinx_serialization}"
    }

    object ktor {
        const val core = "io.ktor:ktor:${Versions.ktor}"
        const val server = "io.ktor:ktor-server-netty:${Versions.ktor}"
        const val logging = "io.ktor:ktor-client-logging:${Versions.ktor}"
        const val websockets = "io.ktor:ktor-websockets:${Versions.ktor}"
        const val serialization = "io.ktor:ktor-serialization:${Versions.ktor}"
        const val auth = "io.ktor:ktor-auth:${Versions.ktor}"
        const val metrics = "io.ktor:ktor-metrics:${Versions.ktor}"
        const val metrics_micrometer = "io.ktor:ktor-metrics-micrometer:${Versions.ktor}"
    }

    const val logback = "ch.qos.logback:logback-classic:${Versions.logback}"
    const val guava = "com.google.guava:guava:${Versions.guava}"
    const val slf4j_api = "org.slf4j:slf4j-api:${Versions.slf4j}"
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val okio = "com.squareup.okio:okio:${Versions.okio}"
    const val metrics_jmx = "io.dropwizard.metrics:metrics-jmx:${Versions.metrics_jmx}"
    const val micrometer_registry_prometheus =
        "io.micrometer:micrometer-registry-prometheus:${Versions.micrometer_registry_prometheus}"
    const val flatlaf = "com.formdev:flatlaf:${Versions.flatlaf}"
    const val sqldelight_sqlite = "com.squareup.sqldelight:sqlite-driver:${Versions.sqldelight}"
    const val sqldelight_coroutines = "com.squareup.sqldelight:coroutines-extensions-jvm:${Versions.sqldelight}"
    const val sqlite = "org.xerial:sqlite-jdbc:${Versions.sqlite}"
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
        "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm",
        "org.jetbrains.kotlinx:kotlinx-serialization-cbor-jvm"
    )
    private val compose = arrayOf(
        "org.jetbrains.compose.animation:animation-core-desktop",
        "org.jetbrains.compose.animation:animation-desktop",
        "org.jetbrains.compose.foundation:foundation-desktop",
//        "org.jetbrains.compose.foundation:foundation-layout-desktop",
        "org.jetbrains.compose.ui:ui-desktop",
        "org.jetbrains.compose.ui:ui-geometry-desktop",
        "org.jetbrains.compose.ui:ui-graphics-desktop",
        "org.jetbrains.compose.ui:ui-text-desktop",
        "org.jetbrains.compose.ui:ui-unit-desktop",
        "org.jetbrains.compose.desktop:desktop-jvm",
        "org.jetbrains.compose.material:material-desktop",
        "org.jetbrains.compose.material:material-icons-core-desktop",
        "org.jetbrains.compose.material:material-icons-extended-desktop"
//        "org.jetbrains.compose.runtime:runtime-desktop"
    )
    val dependencies = arrayOf(
        *kotlinx,
        *ktor,
        *compose,
        "io.micrometer:micrometer-core",
        "io.dropwizard.metrics:metrics-core",
        "ch.qos.logback:logback-core"
    )
}
