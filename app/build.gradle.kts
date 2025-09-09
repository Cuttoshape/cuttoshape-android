plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.example.cuttoshapenew"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cuttoshapenew"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            // proguardFiles can remain here if you want to enable later:
            // proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    androidResources {
        noCompress += "tflite"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.navigation.compose)
    implementation (libs.androidx.runtime.livedata)
    implementation (libs.androidx.runtime)
    implementation ("androidx.compose.ui:ui:1.8.2") // For Modifier and clip
    implementation(libs.okhttp)
    implementation (libs.ktor.client.core)
    implementation (libs.ktor.client.cio) // For JVM client
    implementation (libs.ktor.client.content.negotiation)
    implementation(libs.json)
    implementation (libs.hilt.android)
    // Hilt
    kapt (libs.hilt.android.compiler)
// Hilt for Compose
    implementation (libs.androidx.hilt.navigation.compose)
    implementation (libs.ktor.serialization.kotlinx.json)
    implementation (libs.androidx.foundation.v160)
    implementation(libs.volley)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.retrofit)
    implementation (libs.coil.compose)
    implementation(libs.converter.gson)
    implementation(libs.androidx.datastore.preferences)
    implementation (libs.gson)
    // OkHttp for logging (optional, useful for debugging)
    implementation(libs.logging.interceptor)
    // Coroutines for asynchronous calls
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp) // Add this line
    implementation(libs.logging.interceptor.v4120)
    implementation("com.stripe:stripe-android:20.47.0")
    implementation("com.stripe:financial-connections:20.47.0")

    implementation ("androidx.multidex:multidex:2.0.1")

    // ML Kit deps (kept as you had them)
    implementation ("com.google.mlkit:barcode-scanning:17.3.0")
    implementation ("com.google.mlkit:object-detection:17.0.2")
    implementation ("com.google.mlkit:object-detection-custom:17.0.2")
    implementation ("com.google.mlkit:face-detection:16.1.7")
    implementation ("com.google.mlkit:text-recognition:16.0.1")
    implementation ("com.google.mlkit:text-recognition-chinese:16.0.1")
    implementation ("com.google.mlkit:text-recognition-devanagari:16.0.1")
    implementation( "com.google.mlkit:text-recognition-japanese:16.0.1")
    implementation ("com.google.mlkit:text-recognition-korean:16.0.1")
    implementation ("com.google.mlkit:image-labeling:17.0.9")
    implementation ("com.google.mlkit:image-labeling-custom:17.0.3")
    implementation ("com.google.mlkit:pose-detection:18.0.0-beta5")
    implementation ("com.google.mlkit:pose-detection-accurate:18.0.0-beta5")
    implementation ("com.google.mlkit:segmentation-selfie:16.0.0-beta6")
    implementation ("com.google.mlkit:camera:16.0.0-beta3")
    implementation ("com.google.mlkit:face-mesh-detection:16.0.0-beta3")
    implementation ("com.google.android.gms:play-services-mlkit-subject-segmentation:16.0.0-beta1")
    implementation ("com.google.guava:guava:27.1-android")

    // AndroidX / CameraX stable (you already switched to these)
    implementation ("androidx.camera:camera-core:1.3.4")
    implementation ("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")
    implementation ("androidx.camera:camera-view:1.3.4")

    // Testing, lifecycle, etc (kept)
    androidTestImplementation ("androidx.test:core:1.4.0")
    androidTestImplementation ("androidx.test:runner:1.4.0")
    androidTestImplementation ("androidx.test:rules:1.4.0")
    androidTestImplementation ("androidx.test.ext:junit:1.1.3")

    implementation ("androidx.lifecycle:lifecycle-livedata:2.9.3")
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.3.1")

    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("androidx.annotation:annotation:1.2.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")

    implementation ("com.google.android.odml:image:1.0.0-beta1")


}
configurations.all {
    exclude(group = "com.google.guava", module = "listenablefuture")
}