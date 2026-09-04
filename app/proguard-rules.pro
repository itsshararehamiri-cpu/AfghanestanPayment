# =============================================================================
# AfghanestanPayment — release shrink/obfuscate rules
# =============================================================================

# Stack traces in crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# =============================================================================
# Kotlin / Coroutines
# =============================================================================
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# =============================================================================
# Hilt / Dagger (supplements rules bundled with hilt-android)
# =============================================================================
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
-keep @dagger.hilt.EntryPoint class *
-keep @dagger.hilt.InstallIn class *
-keep class **_HiltModules { *; }
-keep class **_HiltModules$* { *; }
-keep class **_Factory { *; }
-keep class **_MembersInjector { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
}

# =============================================================================
# Gson — transaction result JSON passed between screens
# =============================================================================
-keep class com.google.gson.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.danesh.api.TransactionResultDetail { *; }
-keep enum com.danesh.api.TransactionType { *; }

# =============================================================================
# Room (also see :core:database consumer-rules.pro)
# =============================================================================
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# =============================================================================
# jPOS ISO8583 (also see :connection:iso consumer-rules.pro)
# =============================================================================
-keep class org.jpos.** { *; }
-keepclassmembers class org.jpos.** { *; }
-dontwarn org.jpos.**
-dontwarn org.apache.**
-dontwarn org.osgi.**
-dontwarn javax.jms.**
-dontwarn javax.management.**
-dontwarn javax.naming.**

# jPOS transitive deps — desktop/JVM-only (jline, sleepycat JE, beanshell)
-dontwarn java.awt.**
-dontwarn javax.script.**
-dontwarn javax.transaction.xa.**
-dontwarn java.lang.management.**
-dontwarn jline.**
-dontwarn com.sleepycat.**
-dontwarn bsh.**

-keep class com.danesh.iso.** { *; }
-keepclassmembers class com.danesh.iso.packager.** {
    protected org.jpos.iso.ISOFieldPackager[] fld;
}
-keepclassmembers class * extends org.jpos.iso.ISOBasePackager {
    protected org.jpos.iso.ISOFieldPackager[] fld;
}

# =============================================================================
# Centerm K9 POS SDK (also see :device:KNine consumer-rules.pro)
# =============================================================================
-keep class com.pos.** { *; }
-dontwarn com.pos.**

# =============================================================================
# WorkManager + Hilt Work
# =============================================================================
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class androidx.hilt.work.** { *; }

# =============================================================================
# CameraX
# =============================================================================
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# =============================================================================
# Coil / ZXing / Persian date / ThreeTen
# =============================================================================
-dontwarn coil.**
-dontwarn com.google.zxing.**
-keep class com.github.samanzamani.** { *; }
-keep class saman.zamani.persiandate.** { *; }
-keep class com.jakewharton.threetenabp.** { *; }
-keep class org.threeten.bp.** { *; }

# =============================================================================
# Enums, Parcelable, native
# =============================================================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclasseswithmembernames class * {
    native <methods>;
}

# =============================================================================
# App entry points & BuildConfig
# =============================================================================
-keep class com.danesh.afghanestanpayment.MyApp { *; }
-keep class com.danesh.afghanestanpayment.BootReceiver { *; }
-keep class com.danesh.afghanestanpayment.BootLaunchService { *; }
-keep class com.danesh.afghanestanpayment.BootLaunchActivity { *; }
-keep class com.danesh.afghanestanpayment.BuildConfig { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * {
    <init>(...);
}

# PSP implementations selected at compile time per flavor
-keep class com.danesh.bp.** { *; }
-keep class com.danesh.hp.** { *; }
