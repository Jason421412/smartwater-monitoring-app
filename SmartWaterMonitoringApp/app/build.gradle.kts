import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
}

// Read secrets from local.properties — this file is gitignored and never committed.
// Cloners: create SmartWaterMonitoringApp/local.properties and add:
//   MAPS_API_KEY=your_google_maps_api_key_here
val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(FileInputStream(f))
}


android {
    namespace = "com.smartwater.monitoring"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.smartwater.monitoring"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject Google Maps API key from local.properties into AndroidManifest.xml
        // The manifest uses ${MAPS_API_KEY} — see meta-data entry for com.google.android.geo.API_KEY
        manifestPlaceholders["MAPS_API_KEY"] =
            localProperties.getProperty("MAPS_API_KEY") ?: ""
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // MPAndroidChart for charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Bluetooth
    implementation("androidx.core:core:1.12.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation(project(":flutter"))
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Google Play Services for Location
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")


}



