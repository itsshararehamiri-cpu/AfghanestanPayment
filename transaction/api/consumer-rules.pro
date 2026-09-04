# Gson models passed as JSON between feature screens
-keep class com.danesh.api.TransactionResultDetail { *; }
-keep enum com.danesh.api.TransactionType { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
