import org.gradle.api.JavaVersion

internal object VoidBuildConstants {
    const val COMPILE_SDK = 36
    const val MIN_SDK = 26
    const val TARGET_SDK = 36
    val JAVA_VERSION = JavaVersion.VERSION_17

    /** Flavor dimension shared by every app module: which device the build targets. */
    const val DEV_TYPE_DIMENSION = "dev_type"
    val DEV_TYPES = listOf("softwarekeyboard", "blackberry", "minimalphone")
}
