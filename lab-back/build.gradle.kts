plugins {
    alias(libs.plugins.android.library)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.back"
}

dependencies {
    implementation(project(":lab-contracts"))
    implementation(libs.core.ktx)
}
