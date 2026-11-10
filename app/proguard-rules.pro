# FocusForge ProGuard / R8 Rules

# Room Database
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public void clearAllTables();
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Dagger / Hilt
-keep class * extends androidx.hilt.work.WorkerAssistedFactory
-keepclassmembers class * {
    @javax.inject.* *;
    @dagger.* *;
}

# Gson Rules for Data Export / Import
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.focusforge.app.domain.model.** { *; }
-keep class com.focusforge.app.data.local.entity.** { *; }

# Google Play Billing Library
-keep class com.android.billingclient.api.** { *; }

# Google Mobile Ads (AdMob)
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep Compose Preview and Runtime
-keep class androidx.compose.material3.** { *; }
