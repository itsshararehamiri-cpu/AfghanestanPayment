plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}


android {
    namespace = "com.danesh.menu"
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
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.activity.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    api(libs.androidx.lifecycle.viewmodel.compose)
    api(libs.lifecycle.runtime.compose)
    api(libs.androidx.hilt.navigation.compose)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.navigation.compose)
    api(libs.androidx.lifecycle.runtime.compose)
    api(libs.hilt.navigation.compose)
    api(libs.constraintlayout)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    api(libs.persiandate)
    api(libs.compose.persian.date.picker)
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.material)
    api(libs.junit)
    api(libs.androidx.junit)
    api(libs.androidx.espresso.core)
    api(libs.androidx.lifecycle.runtime.compose)//
    api(platform(libs.androidx.compose.bom))
    api(libs.ui)
    api(libs.ui.tooling.preview)
    api(libs.material3)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    api(libs.constraintlayout)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    api("androidx.compose.material:material-icons-extended")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")
    api(project(":feature:common"))
    implementation(project(":transaction:api"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))
}