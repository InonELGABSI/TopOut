import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("co.touchlab.skie") version "0.10.1"
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
    id("com.github.ben-manes.versions") version "0.51.0"

}

kotlin {
    sqldelight {
        databases {
            create("AppDatabase") {
                packageName.set("com.topout.kmp")
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
            linkerOpts.addAll(listOf("-framework", "CoreMotion"))
        }
    }


    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.ktor.client.okhttp)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)

            implementation(libs.android.driver)
            implementation(project.dependencies.platform(libs.firebase.bom))

            implementation(libs.play.services.location)
            // optional – turns Task<T> into suspend functions
            implementation(libs.kotlinx.coroutines.play.services)

        }
        commonMain.dependencies {

            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.test)


            implementation(libs.firebase.firestore)
            implementation(libs.firebase.common)
            implementation(libs.firebase.auth)
            implementation(project.dependencies.platform(libs.firebase.bom))

            implementation(libs.kotlinx.datetime.v041)

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kermit)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            implementation(libs.runtime)
            implementation(libs.coroutines.extensions)


        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.native.driver)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.topout.kmp.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

