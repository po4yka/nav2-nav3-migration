plugins {
    alias(libs.plugins.android.library)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.deeplink"
}

dependencies {
    implementation(project(":lab-contracts"))
    implementation(libs.core.ktx)
}
