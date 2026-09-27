# Gson models passed as JSON between feature screens
-keep class com.danesh.api.TransactionResultDetail { *; }
-keep enum com.danesh.api.TransactionType { *; }

# Charge list models read when voucher and top-up screens open
-keep class com.danesh.api.ChargeKind { *; }
-keep class com.danesh.api.ChargeOperator { *; }
-keep class com.danesh.api.ChargeProduct { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
