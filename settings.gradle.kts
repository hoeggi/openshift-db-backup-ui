enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
// enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}
rootProject.name = "openshift-db-gui"

include(
    ":app",
    ":process:oc",
    ":process:process",
    ":process:postgres",
    ":server",
    "postgres-auth",
    ":api:api",
    ":api:api-models",
    ":viewmodel",
    ":ui:ui",
    ":i18n",
    ":errorhandler",
    ":settings",
    ":log:transaction-log",
    ":log:sys-log",
    ":build-config",
    ":routes"
)

// dependencyResolutionManagement {
//    versionCatalogs {
//        create("libs") {
//            version("sqlite", "3.34.0")
//            version("sqldelight", "1.4.4")
//            alias("sqlite").to("org.xerial", "sqlite-jdbc").versionRef("sqlite")
//            alias("sqldelight-plugin").to("com.squareup.sqldelight", "gradle-plugin").versionRef("sqldelight")
//            alias("sqldelight-driver").to("com.squareup.sqldelight", "sqlite-driver").versionRef("sqldelight")
//            alias("sqldelight-coroutines").to("com.squareup.sqldelight", "coroutines-extensions-jvm").versionRef("sqldelight")
//
//            bundle("sqldelight", listOf("sqldelight-driver", "sqldelight-coroutines"))
//        }
//    }
// }
