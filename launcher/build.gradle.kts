plugins {
    id("voidlauncher.android.application")
}

android {
    namespace = "dev.coletz.voidlauncher"

    defaultConfig {
        applicationId = "dev.coletz.voidlauncher"
        versionCode = 4
        versionName = "0.4"
    }
}

dependencies {
    implementation(project(":core"))
}
