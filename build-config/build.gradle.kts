plugins {
    kotlin("jvm")
    id("com.github.gmazzo.buildconfig") version Versions.buildconfig
}

buildConfig {
    packageName("io.github.hoeggi.openshiftdb")
    className("BuildConfig")
    useKotlinOutput()
    buildConfigField("String", "APP_NAME", "\"${rootProject.name}\"")
}
