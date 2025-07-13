plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id ("dagger.hilt.android.plugin")
    id ("kotlin-kapt")

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
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
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
    implementation (libs.ktor.client.core)
    implementation (libs.ktor.client.cio) // For JVM client
    implementation (libs.ktor.client.content.negotiation)
    // Hilt
    implementation (libs.hilt.android)
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
}