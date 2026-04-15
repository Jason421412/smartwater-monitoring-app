pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // ✅ allow Flutter plugin to add repo without failing the build
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()

        // JitPack
        maven { url = uri("https://jitpack.io") }

        // ✅ Flutter engine artifacts repo (必须加，否则找不到 flutter_embedding_debug 等)
        maven { url = uri("https://storage.googleapis.com/download.flutter.io") }
    }
}

rootProject.name = "SmartWater Monitoring App"
include(":app")

// ✅ Flutter module include (Add-to-App)
// 注意：请确认 "smartwater_flutter" 与你实际的 Flutter 模块文件夹名称一致，且位于当前安卓项目的上一级目录
val flutterProjectPath = rootDir.parentFile.resolve("smartwater_flutter/.android/include_flutter.groovy")
if (flutterProjectPath.exists()) {
    apply(from = flutterProjectPath)
} else {
    println("WARNING: Flutter module not found at: $flutterProjectPath")
}