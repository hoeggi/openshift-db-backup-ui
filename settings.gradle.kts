pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }

}
rootProject.name = "openshift-db-gui"

include(
    ":process",
    ":oc",
    ":server",
    ":postgres",
    ":api",
    ":viewmodel",
    ":api_models"
)
