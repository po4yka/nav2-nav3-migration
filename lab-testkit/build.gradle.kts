plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.navigationlab.testkit"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    implementation(project(":lab-contracts"))
    implementation(project(":lab-engine"))
    implementation(project(":lab-host-fragment"))
    implementation(project(":lab-host-nav2"))
    implementation(project(":lab-host-nav3"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.runtime)
    implementation(libs.appcompat)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation(libs.junit)

    debugImplementation(libs.compose.ui.test.manifest)
}
