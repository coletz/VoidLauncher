import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Shared configuration for the app modules (`:launcher`, `:spotlight`): SDK levels, signing,
 * build types and the `dev_type` flavors. Each module still sets its own namespace,
 * applicationId and version.
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.application")

        extensions.configure<ApplicationExtension> {
            compileSdk = VoidBuildConstants.COMPILE_SDK

            defaultConfig {
                minSdk = VoidBuildConstants.MIN_SDK
                targetSdk = VoidBuildConstants.TARGET_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = VoidBuildConstants.JAVA_VERSION
                targetCompatibility = VoidBuildConstants.JAVA_VERSION
            }

            signingConfigs {
                create("release") {
                    storeFile = file(property("CLZ_RELEASE_STORE_FILE") as String)
                    storePassword = property("CLZ_RELEASE_STORE_PASSWORD") as String
                    keyAlias = property("CLZ_RELEASE_KEY_ALIAS") as String
                    keyPassword = property("CLZ_RELEASE_KEY_PASSWORD") as String
                }
            }

            buildTypes {
                getByName("debug") {
                    applicationIdSuffix = ".dev"
                    versionNameSuffix = " Debug"
                }
                getByName("release") {
                    isMinifyEnabled = true
                    isShrinkResources = true
                    proguardFiles(
                        getDefaultProguardFile("proguard-android-optimize.txt"),
                        "proguard-rules.pro"
                    )
                    signingConfig = signingConfigs.getByName("release")
                }
            }

            flavorDimensions += VoidBuildConstants.DEV_TYPE_DIMENSION
            productFlavors {
                VoidBuildConstants.DEV_TYPES.forEach { devType ->
                    create(devType) {
                        dimension = VoidBuildConstants.DEV_TYPE_DIMENSION
                    }
                }
            }
        }
    }
}
