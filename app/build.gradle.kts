plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab"

    defaultConfig {
        applicationId = "com.example.navigationlab"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(project(":lab-catalog-wiring"))
    implementation(project(":lab-catalog"))
    implementation(project(":lab-contracts"))
    implementation(project(":lab-engine"))
    implementation(project(":lab-deeplink"))
    implementation(project(":lab-back"))
    implementation(project(":lab-results"))

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)

    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.core.ktx)
    implementation(libs.appcompat)

    testImplementation(project(":lab-recipes"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.test.core)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
