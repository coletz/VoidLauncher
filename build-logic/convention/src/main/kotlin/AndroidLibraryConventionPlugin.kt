import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/** Shared configuration for library modules (`:core`). Libraries have no flavors. */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")

        extensions.configure<LibraryExtension> {
            compileSdk = VoidBuildConstants.COMPILE_SDK

            defaultConfig {
                minSdk = VoidBuildConstants.MIN_SDK
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = VoidBuildConstants.JAVA_VERSION
                targetCompatibility = VoidBuildConstants.JAVA_VERSION
            }
        }
    }
}
