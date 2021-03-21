import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation(compose.desktop.currentOs)
    implementation(kotlin("stdlib"))
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
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            packageName = rootProject.name
            modules("java.logging", "java.naming", "java.management")
        }
    }
}
