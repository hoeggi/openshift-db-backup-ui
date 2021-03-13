import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Versions.kotlin
    id("org.jetbrains.compose") version Versions.compose
    id("com.github.ben-manes.versions") version "0.38.0"
    id("com.autonomousapps.dependency-analysis") version "0.71.0"
    id("me.tylerbwong.gradle.metalava") version "0.1.6"
}

group = "io.github.hoeggi"
version = "1.0.0"

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

dependencyAnalysis {
    issues {
        all {
            onUsedTransitiveDependencies {
                exclude(*TransitivDependencies.dependencies)
            }
        }
    }
}


dependencies {
    testImplementation(kotlin("test-junit"))
    implementation(compose.desktop.currentOs)
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":server"))
    implementation(project(":ui"))
    implementation(Dependencies.logback)
    implementation(Dependencies.flatlaf)
    compileOnly(Dependencies.slf4j_api)
}

tasks.test {
    useJUnit()
}

compose.desktop {
    application {
        mainClass = "io.github.hoeggi.openshiftdb.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            packageName = rootProject.name
            modules("java.logging", "java.naming", "java.management")
        }
    }
}

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        jcenter {
            content {
                includeModule("net.rubygrapefruit", "ansi-control-sequence-util")
                includeModule("org.jetbrains.kotlinx", "kotlinx-collections-immutable")
                includeModule("org.jetbrains.kotlinx", "kotlinx-collections-immutable-jvm")
                includeModule("org.jetbrains.trove4j", "trove4j")
            }
        }
        maven {
            url = uri("https://maven.google.com")
            content {
                includeModule("com.android.tools.external.com-intellij", "kotlin-compiler")
            }
        }
    }

    tasks.withType<KotlinCompile> {
//        kotlinOptions.useIR = true
        kotlinOptions.jvmTarget = "15"
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xuse-experimental=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xuse-experimental=androidx.compose.material.ExperimentalMaterialApi",
            "-Xuse-experimental=kotlin.io.path.ExperimentalPathApi",
            "-Xuse-experimental=androidx.compose.animation.ExperimentalAnimationApi",
            "-Xinline-classes"
        )
    }
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
            }
        }
    }
}