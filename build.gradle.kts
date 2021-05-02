import com.autonomousapps.DependencyAnalysisExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

buildscript {
    repositories {
        maven("https://plugins.gradle.org/m2/")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    dependencies {
        classpath(Plugins.compose)
        classpath(Plugins.gradle_versions)
        classpath(Plugins.dependency_analysis)
        classpath(Plugins.module_dependency_graph)
        classpath(Plugins.spotless)
        classpath(kotlin("gradle-plugin", version = Versions.kotlin))
    }
}

apply(plugin = "com.diffplug.spotless")
apply(plugin = "com.savvasdalkitsis.module-dependency-graph")
apply(plugin = "com.autonomousapps.dependency-analysis")
apply(plugin = "com.github.ben-manes.versions")

// apply(plugin = "all-projects")
// apply {
//    from("https://raw.githubusercontent.com/JakeWharton/SdkSearch/master/gradle/projectDependencyGraph.gradle")
// }

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

configure<SpotlessExtension> {
    kotlin {
        target("**/src/**/*.kt", "**/src/**/*.kt")
        ktlint("0.38.0").userData(mapOf("indent_size" to "4", "continuation_indent_size" to "2"))
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("0.38.0").userData(mapOf("indent_size" to "4", "continuation_indent_size" to "2"))
        trimTrailingWhitespace()
        endWithNewline()
    }
}

configure<DependencyAnalysisExtension> {
    issues {
        all {
            onUsedTransitiveDependencies {
                exclude(*TransitivDependencies.dependencies)
            }
        }
    }
}

subprojects {
    group = "io.github.hoeggi"
    version = "1.0.0"

    configurations.all {
        resolutionStrategy.eachDependency {
            when {
                requested.name.startsWith("kotlinx-serialization") -> {
                    if (requested.version != Versions.kotlinx_serialization) {
                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.kotlinx_serialization}")
                        useVersion(Versions.kotlinx_serialization)
                    }
                }
                requested.name.startsWith("kotlinx-coroutines") -> {
                    if (requested.version != Versions.kotlinx_coroutines) {
                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.kotlinx_coroutines}")
                        useVersion(Versions.kotlinx_coroutines)
                    }
                }
                requested.name.startsWith("kotlin-") -> {
                    if (requested.group == "org.jetbrains.kotlin" && requested.version != Versions.kotlin) {
                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.kotlin}")
                        useVersion(Versions.kotlin)
                    }
                }
                requested.name.startsWith("okio") -> {
                    if (requested.version != Versions.okio) {
                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.okio}")
                        useVersion(Versions.okio)
                    }
                }
                requested.name.startsWith("sqlite-jdbc") -> {

                    if (requested.version != Versions.sqlite) {
                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.sqlite}")
                        useVersion(Versions.sqlite)
                    }
                }
                requested.name.startsWith("slf4j-api") -> {
                    if (requested.version != Versions.slf4j) {
                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.slf4j}")
                        useVersion(Versions.slf4j)
                    }
                }
                requested.name.startsWith("kotlinx-collections-immutable") -> {
                    if (requested.version != Versions.slf4j) {
                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to 0.3.4")
                        useVersion("0.3.4")
                    }
                }
            }
        }
    }

    repositories {
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.useIR = true
        kotlinOptions.jvmTarget = "15"
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xuse-experimental=androidx.compose.material.ExperimentalMaterialApi",
            "-Xuse-experimental=kotlin.io.path.ExperimentalPathApi",
            "-Xuse-experimental=androidx.compose.animation.ExperimentalAnimationApi",
            "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi",
            "-Xinline-classes",
            "-no-reflect",
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
//            "-Xexplicit-api=strict"
        )
    }
}
