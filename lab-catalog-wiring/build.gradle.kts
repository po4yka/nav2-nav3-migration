plugins {
    alias(libs.plugins.android.library)
    id("navigationlab.android-base")
}

android {
    namespace = "com.example.navigationlab.catalog.wiring"
}

dependencies {
    implementation(project(":lab-catalog"))
    implementation(project(":lab-contracts"))
    implementation(project(":lab-host-fragment"))
    implementation(project(":lab-host-nav2"))
    implementation(project(":lab-host-nav3"))
    implementation(project(":lab-recipes"))
}
