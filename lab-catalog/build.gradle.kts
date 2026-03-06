plugins {
    alias(libs.plugins.android.library)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.catalog"
}

dependencies {
    implementation(project(":lab-contracts"))
}
