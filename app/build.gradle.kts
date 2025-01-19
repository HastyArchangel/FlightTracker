import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.lang.System.getProperty

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

val flightAwareApiKey: String = gradleLocalProperties(rootDir, providers)
    .getProperty("FLIGHTAWARE_API_KEY", "")

android {
    namespace = "com.example.FlightTracker"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.FlightTracker"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "FLIGHTAWARE_API_KEY", "\"$flightAwareApiKey\"")
        }
        release {
            buildConfigField("String", "FLIGHTAWARE_API_KEY", "\"$flightAwareApiKey\"")
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
    implementation(platform(libs.firebase.bom))
    implementation (libs.firebase.messaging.ktx)
    implementation (libs.google.services)
    implementation (libs.ui)
    implementation (libs.androidx.navigation.compose)
    implementation (libs.maps.compose)
    implementation (libs.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.play.services.maps)
//    implementation (libs.play.services.maps)
//    implementation (libs.retrofit2.retrofit)
//    implementation (libs.converter.gson)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}