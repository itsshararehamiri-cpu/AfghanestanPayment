plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.danesh.support"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.lifecycle.runtime.compose)
    api(libs.androidx.hilt.navigation.compose)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.navigation.compose)
    api(libs.hilt.navigation.compose)
    api(platform(libs.androidx.compose.bom))
    api(libs.ui)
    api(libs.ui.tooling.preview)
    api(libs.material3)
    debugImplementation(libs.ui.tooling)
    implementation(libs.android.gson)
    implementation(project(":core:ui"))
    implementation(project(":feature:common"))
    implementation(project(":transaction:api"))
    implementation(project(":device:core"))
    implementation(project(":feature:common"))
}
