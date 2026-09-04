plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    id("com.google.dagger.hilt.android") version "2.56.1" apply false
   // alias(libs.plugins.compose.compiler) apply false
   // id ("io.sentry.android.gradle") version "4.12.0" // یا آخرین نسخه
}