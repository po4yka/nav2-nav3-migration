plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.navigationlab.deeplink"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":lab-contracts"))
    implementation(libs.core.ktx)
}
