plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "dev.coletz.voidlauncher"
    compileSdk = 36

    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "dev.coletz.voidlauncher"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "0.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
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

    flavorDimensions += listOf("dev_type", "app_type")
    productFlavors {
        create("softwarekeyboard") {
            dimension = "dev_type"
        }

        create("blackberry") {
            dimension = "dev_type"
        }

        create("minimalphone") {
            dimension = "dev_type"
        }

        create("launcher") {
            dimension = "app_type"
            buildConfigField("boolean", "IS_LAUNCHER", "true")
        }

        create("spotlight") {
            dimension = "app_type"
            applicationIdSuffix = ".spotlight"
            buildConfigField("boolean", "IS_LAUNCHER", "false")
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

