//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
//
//subprojects {
//    group = "io.github.hoeggi"
//    version = "1.0.0"
//
////    apply(plugin = "me.tylerbwong.gradle.metalava")
//
//    configurations.all {
//        resolutionStrategy.eachDependency {
//            when {
//                requested.name.startsWith("kotlinx-serialization") -> {
//                    if (requested.version != Versions.kotlinx_serialization) {
//                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.kotlinx_serialization}")
//                        useVersion(Versions.kotlinx_serialization)
//                    }
//                }
//                requested.name.startsWith("kotlinx-coroutines") -> {
//                    if (requested.version != Versions.kotlinx_coroutines) {
//                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.kotlinx_coroutines}")
//                        useVersion(Versions.kotlinx_coroutines)
//                    }
//                }
//                requested.name.startsWith("kotlin-") -> {
//                    if (requested.group == "org.jetbrains.kotlin" && requested.version != Versions.kotlin) {
//                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.kotlin}")
//                        useVersion(Versions.kotlin)
//                    }
//                }
//                requested.name.startsWith("okio") -> {
//                    if (requested.version != Versions.okio) {
//                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.okio}")
//                        useVersion(Versions.okio)
//                    }
//                }
//                requested.name.startsWith("sqlite-jdbc") -> {
//                    if (requested.version != Versions.sqlite) {
//                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.sqlite}")
//                        useVersion(Versions.sqlite)
//                    }
//                }
//                requested.name.startsWith("slf4j-api") -> {
//                    if (requested.version != Versions.slf4j) {
//                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to ${Versions.slf4j}")
//                        useVersion(Versions.slf4j)
//                    }
//                }
//                requested.name.startsWith("kotlinx-collections-immutable") -> {
//                    if (requested.version != Versions.slf4j) {
//                        println("overriding ${requested.group}:${requested.name} version from ${requested.version} to 0.3.4")
//                        useVersion("0.3.4")
//                    }
//                }
//            }
//        }
//    }
//
//    repositories {
//        mavenCentral()
//        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
//    }
//
//    tasks.withType<KotlinCompile> {
//        kotlinOptions.useIR = true
//        kotlinOptions.jvmTarget = "15"
//        kotlinOptions.freeCompilerArgs += listOf(
//            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
//            "-Xuse-experimental=androidx.compose.foundation.ExperimentalFoundationApi",
//            "-Xuse-experimental=androidx.compose.material.ExperimentalMaterialApi",
//            "-Xuse-experimental=kotlin.io.path.ExperimentalPathApi",
//            "-Xuse-experimental=androidx.compose.animation.ExperimentalAnimationApi",
//            "-Xuse-experimental=kotlinx.serialization.ExperimentalSerializationApi",
//            "-Xinline-classes",
//            "-no-reflect",
//            "-P",
//            "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
////            "-Xexplicit-api=strict"
//        )
//    }
//}
