# Keep Activity subclasses referenced from AndroidManifest.xml
-keep class com.example.navigationlab.** extends android.app.Activity

# Keep @Serializable classes used as Nav3 typed keys
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keep @kotlinx.serialization.Serializable class *

# Keep Koin modules
-keep class com.example.navigationlab.** extends org.koin.core.module.Module
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# Compose: keep Composable functions from being removed
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
