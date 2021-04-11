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
apply(plugin = "all-projects")
// apply {
//    from("https://raw.githubusercontent.com/JakeWharton/SdkSearch/master/gradle/projectDependencyGraph.gradle")
// }

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
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
