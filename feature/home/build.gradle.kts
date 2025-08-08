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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}