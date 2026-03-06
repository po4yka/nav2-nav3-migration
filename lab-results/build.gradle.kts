plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.results"

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":lab-contracts"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
}
