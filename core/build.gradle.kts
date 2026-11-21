plugins {
    id("voidlauncher.android.library")
    alias(libs.plugins.google.ksp)
}

android {
    namespace = "dev.coletz.voidlauncher.core"
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // UI
    api(libs.androidx.appcompat)
    api(libs.androidx.constraintlayout)
    api(libs.androidx.recyclerview)
    api(libs.androidx.viewpager2)

    // Room
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Lifecycle
    api(libs.androidx.lifecycle.extensions)
    api(libs.androidx.lifecycle.common.java8)

    // ViewModel
    api(libs.androidx.lifecycle.viewmodel.ktx)

    // Coroutines
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.coroutines.android)

    api(libs.androidx.preference.ktx)
}
