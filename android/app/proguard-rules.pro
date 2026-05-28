# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Ktor
-keep class io.ktor.** { *; }

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
