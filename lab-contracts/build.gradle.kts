plugins {
    alias(libs.plugins.android.library)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.contracts"
}

dependencies {
    implementation(libs.core.ktx)
}
