enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

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
    ":process",
    ":oc",
    ":server",
    ":postgres",
    "postgres-auth",
    ":api:api",
    ":api:api-models",
    ":viewmodel",
    ":ui",
    ":i18n",
    ":errorhandler",
    ":settings",
    ":log:transaction-log",
    ":log:sys-log",
    ":build-config",
    ":routes"
)
