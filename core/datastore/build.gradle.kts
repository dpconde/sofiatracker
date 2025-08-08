plugins {
    id("sofiatracker.android.library")
    id("sofiatracker.android.hilt")
}

android {
    namespace = "com.dpconde.sofiatracker.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)
    api(projects.core.model)
    implementation(projects.core.common)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}