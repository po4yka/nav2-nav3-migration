plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.navigationlab.host.fragment"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(project(":lab-contracts"))

    implementation(libs.appcompat)
    implementation(libs.fragment.ktx)
    implementation(libs.core.ktx)

    // Compose deps for T4 dual-container topology (ComposeView + overlay FrameLayout)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)

    // Nav2 for T6 topology (Fragment -> ComposeView -> internal Nav2)
    implementation(libs.nav2.compose)

    // Nav3 for B08 (Fragment -> ComposeView -> Nav3 NavDisplay with modal)
    implementation(libs.nav3.runtime)
    implementation(libs.nav3.ui)
}
