plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.navigationlab.catalog"
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
    implementation(project(":lab-host-fragment"))
    implementation(project(":lab-host-nav2"))
    implementation(project(":lab-host-nav3"))
    implementation(project(":lab-recipes"))
}
