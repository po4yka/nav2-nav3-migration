plugins {
    alias(libs.plugins.android.library)
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

    testOptions {
        animationsDisabled = true
    }
}

dependencies {
    implementation(project(":lab-catalog"))
    implementation(project(":lab-contracts"))
    implementation(project(":lab-deeplink"))
    implementation(project(":lab-engine"))
    implementation(project(":lab-host-fragment"))
    implementation(project(":lab-host-nav2"))
    implementation(project(":lab-host-nav3"))
    implementation(project(":lab-recipes"))

    implementation(libs.appcompat)

    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.ext.junit)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.test.rules)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.runtime)
    androidTestImplementation(libs.nav3.runtime)
    androidTestImplementation(libs.junit)
}
