plugins {
    id("sofiatracker.android.feature")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.dpconde.sofiatracker.feature.statistics"
    
    buildFeatures {
        compose = true
    }
}

dependencies {

}