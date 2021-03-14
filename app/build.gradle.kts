import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
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



compose.desktop {
    application {
        mainClass = "io.github.hoeggi.openshiftdb.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Rpm)
            packageName = rootProject.name
            modules("java.logging", "java.naming", "java.management")
        }
    }
}
