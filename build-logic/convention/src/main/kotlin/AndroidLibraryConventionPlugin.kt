import com.android.build.gradle.LibraryExtension
import com.dpconde.sofiatracker.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.android")
            }

            extensions.configure<LibraryExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
                
                // Consumer proguard files
                defaultConfig.consumerProguardFiles("consumer-rules.pro")
            }

            dependencies {
                add("testImplementation", kotlin("test"))
            }
        }
    }
}