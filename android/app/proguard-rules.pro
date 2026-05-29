# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# SLF4J (not available on Android, referenced by Ktor)
-dontwarn org.slf4j.**

# Java Management API (not available on Android, referenced by Ktor debug utils)
-dontwarn java.lang.management.**
-dontwarn javax.management.**

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.pickcode.v2.**$$serializer { *; }
-keepclassmembers class com.pickcode.v2.** {
    *** Companion;
}
-keepclasseswithmembers class com.pickcode.v2.** {
    kotlinx.serialization.KSerializer serializer(...);
}
