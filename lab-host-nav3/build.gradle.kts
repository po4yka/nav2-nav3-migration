plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.host.nav3"

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":lab-contracts"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)

    implementation(libs.nav3.runtime)
    implementation(libs.nav3.ui)

    implementation(libs.nav2.compose)

    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.appcompat)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.test.core)
}
