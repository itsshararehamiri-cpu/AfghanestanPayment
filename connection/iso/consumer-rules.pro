# jPOS ISO8583 — reflection-heavy packagers/channels
-keep class org.jpos.** { *; }
-keepclassmembers class org.jpos.** { *; }
-dontwarn org.jpos.**
-dontwarn org.apache.**
-dontwarn org.osgi.**
-dontwarn javax.jms.**
-dontwarn javax.management.**
-dontwarn javax.naming.**

# jPOS transitive deps — desktop/JVM-only
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
