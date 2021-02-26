import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    id("org.jetbrains.compose") version "0.4.0-build168"
    id("org.jetbrains.kotlin.kapt") version "1.4.30"
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
    implementation(compose.materialIconsExtended)
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.squareup.moshi:moshi-kotlin:1.11.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.11.0")
//    implementation("com.github.theapache64:name-that-color:1.0.0-alpha02")

    implementation("com.google.guava:guava:30.1-jre")
    implementation("com.squareup.okhttp3:okhttp:4.9.0")

    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.dropwizard.metrics:metrics-jmx:4.1.17")

    implementation("io.ktor:ktor:1.5.1")
    implementation("io.ktor:ktor-server-netty:1.5.1")

    implementation("io.ktor:ktor-client-core:1.5.1")
    implementation("io.ktor:ktor-client-core-jvm:1.5.1")
    implementation("io.ktor:ktor-client-logging-jvm:1.5.1")

    implementation("io.ktor:ktor-websockets:1.5.1")
    implementation("io.ktor:ktor-auth:1.5.1")
    implementation("io.ktor:ktor-gson:1.5.1")
    implementation("io.ktor:ktor-metrics:1.5.1")
    implementation("io.ktor:ktor-metrics-micrometer:1.5.1")
    implementation("io.micrometer:micrometer-registry-prometheus:1.6.4")

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
            modules("java.logging")
        }
    }
}
