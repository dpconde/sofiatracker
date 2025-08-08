plugins {
    id("sofiatracker.android.library")
    id("sofiatracker.android.hilt")
}

android {
    namespace = "com.dpconde.sofiatracker.core.network"
}

dependencies {
    implementation(projects.core.model)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore.ktx)

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