# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ==================== Retrofit ====================
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# ==================== OkHttp ====================
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# ==================== Gson ====================
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*

# ==================== DTO Classes (Required!) ====================
# Keep all DTO classes to prevent Gson deserialization errors
-keep class com.smartwater.monitoring.network.dto.** { *; }
-keepclassmembers class com.smartwater.monitoring.network.dto.** { *; }

# ==================== MPAndroidChart ====================
-keep class com.github.mikephil.charting.** { *; }

# ==================== Flutter ====================
-keep class io.flutter.** { *; }
-keep class io.flutter.embedding.** { *; }