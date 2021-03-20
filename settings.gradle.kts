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
":api",
":viewmodel",
":api_models",
":ui",
":i18n",
":errorhandler",
":settings",
":transaction-log",
":build-config"
)
include("routes")
