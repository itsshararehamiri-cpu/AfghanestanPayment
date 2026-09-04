import com.android.build.api.dsl.ApplicationProductFlavor
import org.gradle.kotlin.dsl.implementation
import java.util.Properties

val releaseKeystorePath = "../centerm.keystore"
val releaseKeyAlias = "androiddebugkey"
val releaseStorePassword = "android"
val releaseKeyPassword = "android"

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun signingProperty(name: String, gradleFallback: String): String =
    keystoreProperties.getProperty(name)?.takeIf { it.isNotBlank() } ?: gradleFallback

fun releaseSigningProperty(
    gradlePropertyName: String,
    keystorePropertyName: String,
    fallback: String,
): String =
    keystoreProperties.getProperty(keystorePropertyName)?.takeIf { it.isNotBlank() }
        ?: (project.findProperty(gradlePropertyName) as String?)?.takeIf { it.isNotBlank() }
        ?: fallback

// اتصال پیش‌فرض همراه‌پی (HP, AP, PN, Fanava)
val hpDefaultServerIp =  "103.216.160.154"//"46.100.13.79"//
val hpDefaultServerPort = 6232//50503//
val bpDefaultServerIp = "212.16.73.141"
val bpDefaultServerPort = 8585

fun ApplicationProductFlavor.configureHamrahPayCurrency() {
    buildConfigField("String", "DEFAULT_CURRENCY", "\"971\"")
    buildConfigField("String", "CURRENCY_LABEL", "\"AFN\"")
    resValue("string", "currency_label", "AFN")
}
fun ApplicationProductFlavor.configureBehpardakhtCurrency() {
    buildConfigField("String", "DEFAULT_CURRENCY", "\"364\"")
    buildConfigField("String", "CURRENCY_LABEL", "\"ریال\"")
    resValue("string", "currency_label", "ریال")
}
fun ApplicationProductFlavor.configureSadadCurrency() {
    buildConfigField("String", "DEFAULT_CURRENCY", "\"364\"")
    buildConfigField("String", "CURRENCY_LABEL", "\"ریال\"")
    resValue("string", "currency_label", "ریال")
}
fun ApplicationProductFlavor.configureHamrahPayReceiptFee() {
    buildConfigField("String", "DEFAULT_BALANCE_TRANSACTION_FEE", "\"10\"")
}
fun ApplicationProductFlavor.configureBehpardakhtReceiptFee() {
    buildConfigField("String", "DEFAULT_BALANCE_TRANSACTION_FEE", "\"1800\"")
}
fun ApplicationProductFlavor.configureSadadReceiptFee() {
    buildConfigField("String", "DEFAULT_BALANCE_TRANSACTION_FEE", "\"1800\"")
}
fun ApplicationProductFlavor.configureBehpardakhtMenuFeatures() {
    buildConfigField(
        "String",
        "ENABLED_FEATURES",
        "\"PURCHASE,TOPUP,BILL,BALANCE,SETTINGS,REPORT,VOUCHER\"",
    )
}
fun ApplicationProductFlavor.configureSadadMenuFeatures() {
    buildConfigField(
        "String",
        "ENABLED_FEATURES",
        "\"PURCHASE,TOPUP,BILL,BALANCE,SETTINGS,REPORT,VOUCHER\"",
    )
}

fun ApplicationProductFlavor.configureHamrahPayMenuFeatures() {
    buildConfigField(
        "String",
        "ENABLED_FEATURES",
        "\"TRANSFER,CASH_DEPOSIT,CASH_OUT,SETTINGS,REPORT,PURCHASE,BALANCE,BILL\"",
    )
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}
android {
    namespace = "com.danesh.afghanestanpayment"
    compileSdk = 36
    flavorDimensions += listOf("psp", "device")
    productFlavors {
        create("hp") {
            dimension = "psp"
            applicationIdSuffix = ".hp"
            resValue("string", "app_name", "همراه پی")
            buildConfigField("String", "ACTIVE_PSP", "\"HP\"")
            buildConfigField("String", "DEFAULT_SERVER_IP", "\"$hpDefaultServerIp\"")
            buildConfigField("int", "DEFAULT_SERVER_PORT", "$hpDefaultServerPort")
            configureHamrahPayCurrency()
            configureHamrahPayMenuFeatures()
            configureHamrahPayReceiptFee()
            applicationId= "com.danesh.afghanestanpayment.app.hp"

        }
        create("bp") {
            dimension = "psp"
            applicationIdSuffix = ".bp"
            isDefault = true
            resValue("string", "app_name", "به‌پرداخت")
            buildConfigField("String", "ACTIVE_PSP", "\"BP\"")
            buildConfigField("String", "DEFAULT_SERVER_IP", "\"$bpDefaultServerIp\"")
            buildConfigField("int", "DEFAULT_SERVER_PORT", "$bpDefaultServerPort")
            configureBehpardakhtCurrency()
            configureBehpardakhtMenuFeatures()
            configureBehpardakhtReceiptFee()
            applicationId="com.danesh.afghanestanpayment.app.bp"

        }
        create("sadad") {
            dimension = "psp"
            applicationIdSuffix = ".sadad"
            isDefault = true
            resValue("string", "app_name", "سداد")
            buildConfigField("String", "ACTIVE_PSP", "\"SADAD\"")
            buildConfigField("String", "DEFAULT_SERVER_IP", "\"$bpDefaultServerIp\"")
            buildConfigField("int", "DEFAULT_SERVER_PORT", "$bpDefaultServerPort")
            configureSadadCurrency()
            configureSadadMenuFeatures()
            configureSadadReceiptFee()
            applicationId="com.danesh.afghanestanpayment.app.sadad"

        }
        create("K9") {
            dimension = "device"
            buildConfigField("String", "ACTIVE_DEVICE", "\"K9\"")
        }
        create("K10") {
            dimension = "device"
            buildConfigField("String", "ACTIVE_DEVICE", "\"K10\"")
        }
    }
    defaultConfig {
        multiDexEnabled = true
        applicationId = "com.danesh.afghanestanpayment"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "ACTIVE_PROTOCOL", "\"ISO\"")
        buildConfigField("String", "DEFAULT_SERVER_IP", "\"$bpDefaultServerIp\"")
        buildConfigField("int", "DEFAULT_SERVER_PORT", "$bpDefaultServerPort")
        buildConfigField("String", "DEFAULT_CURRENCY", "\"364\"")
        buildConfigField("String", "CURRENCY_LABEL", "\"ریال\"")
        buildConfigField("String", "DEFAULT_BALANCE_TRANSACTION_FEE", "\"5000\"")
        buildConfigField(
            "String",
            "ENABLED_FEATURES",
            "\"PURCHASE,TOPUP,BILL,BALANCE,SETTINGS,REPORT,VOUCHER\"",
        )
    }

//    signingConfigs {
//        getByName("debug") {
//            enableV1Signing = true
//            enableV2Signing = true
//            enableV3Signing = true
//        }
//        create("release") {
//            enableV1Signing = true
//            enableV2Signing = true
//            enableV3Signing = true
//        }
//    }
    signingConfigs {
        create("test_sign") {
            storeFile = file("../testkeystore.jks") // keystore path
            storePassword = "123456" // keystore password
            keyAlias = "testkey"
            keyPassword = "123456"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }
    buildTypes {
        release {
          //  signingConfig = signingConfigs.getByName("release")
//            isMinifyEnabled = true
//            isShrinkResources = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro",
//            )
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 引用签名配置
            signingConfig = signingConfigs.getByName("test_sign")
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
        buildConfig = true
    }
}
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.work.compiler)
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("androidx.hilt:hilt-work:1.0.0")
    implementation("androidx.multidex:multidex:2.0.1")
    add("hpImplementation", project(":psp:hp"))
    add("bpImplementation", project(":psp:bp"))
    implementation(project(":feature:balance"))
    implementation(project(":feature:purchase"))
    implementation(project(":feature:common"))
    implementation(project(":feature:menu"))
    implementation(project(":feature:bill"))
    implementation(project(":feature:topup"))
    implementation(project(":feature:voucher"))
    implementation(project(":feature:support"))
    implementation(project(":feature:report"))
    implementation(project(":feature:splash"))
    implementation(project(":feature:card_to_card"))
    implementation(project(":feature:wallet_to_wallet"))
    implementation(project(":feature:cash_deposit"))
    implementation(project(":feature:cash_out"))
    implementation(project(":feature:settings"))
    implementation(project(":device:core"))
    implementation(project(":device:KNine"))
    implementation(project(":transaction:api"))
    implementation(project(":connection:iso"))
    implementation(project(":connection:core"))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":transaction:engine"))
    implementation(project(":core:ui"))



}