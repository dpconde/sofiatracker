plugins {
    id("sofiatracker.android.feature")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.dpconde.sofiatracker.feature.home"
    
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(projects.core.data) //TODO: this should not be here
}