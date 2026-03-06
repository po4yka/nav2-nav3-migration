plugins {
    alias(libs.plugins.android.library)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.testkit"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.runtime)
    androidTestImplementation(libs.nav3.runtime)
    androidTestImplementation(libs.junit)
}
