import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

dependencies {
//    testImplementation(kotlin("test-junit"))
    implementation(compose.desktop.currentOs)
    implementation(kotlin("stdlib-jdk8"))
    implementation(projects.server)
    implementation(projects.ui.ui)
    implementation(Dependencies.logback)
    implementation(Dependencies.flatlaf)
    compileOnly(Dependencies.slf4j_api)

    //https://github.com/JetBrains/compose-jb/issues/273
//    val os = System.getProperty("os.name")
//    val currentTarget = when {
//        os.equals("Mac OS X", ignoreCase = true) -> "macos"
//        os.startsWith("Win", ignoreCase = true) -> "windows"
//        os.startsWith("Linux", ignoreCase = true) -> "linux"
//        else -> error("Unknown OS name: $os")
//    }
//    runtimeOnly("org.jetbrains.skiko:skiko-jvm-runtime-${currentTarget}-x64:0.2.33")
}



compose.desktop {
    application {
        mainClass = "io.github.hoeggi.openshiftdb.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Deb, TargetFormat.Rpm)
            packageName = rootProject.name
            modules("java.logging", "java.naming", "java.management", "java.sql")
        }
    }
}
