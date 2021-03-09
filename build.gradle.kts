import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("jvm") version Versions.kotlin
    id("org.jetbrains.compose") version Versions.compose
    id("com.github.ben-manes.versions") version "0.38.0"
}

group = "io.github.hoeggi"
version = "1.0.0"

tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    revision = "release"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    jcenter()
    maven { url = uri("https://jitpack.io") }
}


dependencies {
    testImplementation(kotlin("test-junit"))
    implementation(compose.desktop.currentOs)
    implementation(kotlin("stdlib-jdk8"))
    implementation(project(":server"))
    implementation(project(":ui"))
    implementation(project(":errorhandler"))
    compileOnly(Dependencies.slf4j_api)
    implementation(Dependencies.logback)
}

allprojects {
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
