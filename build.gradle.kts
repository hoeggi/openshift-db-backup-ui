import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    id("org.jetbrains.compose") version "0.3.0-build154"
//    id("org.jetbrains.kotlin.kapt") version "1.4.30"
}

group = "io.github.hoeggi"
version = "1.0.0-alpha01"

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
    compileOnly(Dependencies.slf4j_api)
    implementation("ch.qos.logback:logback-classic:1.2.3")
}

//sourceSets {
//    main.kotlin.srcDirs = [ 'src' ]
//    main.resources.srcDirs = [ 'resources' ]
//}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=androidx.compose.foundation.ExperimentalFoundationApi"
}

compose.desktop {
    application {
        mainClass = "io.github.hoeggi.openshiftdb.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb)
            packageName = "openshift-db-gui"
            modules("java.logging", "java.naming", "java.management")
        }
    }
}
