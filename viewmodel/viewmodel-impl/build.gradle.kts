plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    api(projects.errorhandler)
    implementation(projects.api.apiModels)
    implementation(projects.api.api)
    api(projects.viewmodel.viewmodelInterfaces)
    implementation(Dependencies.Kotlin.coroutines)
    implementation(Dependencies.guava)
    compileOnly(Dependencies.slf4j_api)
}
