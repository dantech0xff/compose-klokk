import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

fun setting(name: String): String? =
    (findProperty(name) as String?)?.takeIf { it.isNotBlank() }
        ?: System.getenv(name)?.takeIf { it.isNotBlank() }

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

android {
    namespace = "dev.dantech.klokk"
    compileSdk {
        version = release(37) { minorApiLevel = 1 }
    }
    defaultConfig {
        applicationId = "dev.dantech.klokk"
        minSdk = 24
        targetSdk = 37
        versionCode = setting("KLOKK_VERSION_CODE")?.toInt() ?: 2
        versionName = setting("KLOKK_VERSION_NAME") ?: "1.0"
    }
    val keystorePath = setting("KLOKK_KEYSTORE")
    val keystorePassword = setting("KLOKK_KEYSTORE_PASSWORD")
    val keyAlias = setting("KLOKK_KEY_ALIAS")
    val keyPassword = setting("KLOKK_KEY_PASSWORD")
    if (keystorePath != null && keystorePassword != null && keyAlias != null && keyPassword != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystorePath)
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
}
