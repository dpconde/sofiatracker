plugins {
    id("sofiatracker.android.feature")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.dpconde.sofiatracker.feature.settings"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.data) //Should not be here
}