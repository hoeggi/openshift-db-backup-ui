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
    ":server",
    ":postgres-auth",
    ":i18n",
    ":errorhandler",
    ":settings",
    ":build-config",
    ":routes"
)
include(
    ":api:api",
    ":api:api-models"
)
include(
    ":log:sys-log",
    ":log:transaction-log",
)
include(
    ":process:oc",
    ":process:postgres",
    ":process:process",
)
include(
    ":ui:base-elements",
    ":ui:eventlog",
    ":ui:filechooser",
    ":ui:navigation",
    ":ui:theme",
    ":ui:ui",
    ":ui:viewmodel-extensions",
)
include(
    ":viewmodel:viewmodel-impl",
    ":viewmodel:viewmodel-interfaces"
)
