plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "dev.coletz.voidlauncher"
    compileSdk = 35

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "dev.coletz.voidlauncher"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    signingConfigs {
        create("release") {
            val CLZ_RELEASE_STORE_FILE: String by project
            val CLZ_RELEASE_STORE_PASSWORD: String by project
            val CLZ_RELEASE_KEY_ALIAS: String by project
            val CLZ_RELEASE_KEY_PASSWORD: String by project
            storeFile = file(CLZ_RELEASE_STORE_FILE)
            storePassword = CLZ_RELEASE_STORE_PASSWORD
            keyAlias = CLZ_RELEASE_KEY_ALIAS
            keyPassword = CLZ_RELEASE_KEY_PASSWORD
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".dev"
            versionNameSuffix = " Debug"
        }

        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "env"
    productFlavors {
        create("softwarekeyboard") {
            dimension = "env"
        }

        create("blackberry") {
            dimension = "env"
        }

        create("minimalphone") {
            dimension = "env"
        }
    }

    val BASE_APP_NAME = "Void Launcher"

    applicationVariants.all {
        if (buildType.name == "debug") {
            val newLabel = when (flavorName) {
                "blackberry" -> "$BASE_APP_NAME for BB"
                "minimalphone" -> "$BASE_APP_NAME for Minimal"
                else -> BASE_APP_NAME
            }
            resValue("string", "app_name", newLabel)
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // UI
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.viewpager2)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Lifecycle
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.common.java8)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Coroutines
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.preference.ktx)
}

