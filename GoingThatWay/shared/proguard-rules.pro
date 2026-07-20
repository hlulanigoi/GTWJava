# Keep Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# Keep OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Gson
-keep class com.google.gson.** { *; }
-keep class com.goinghatway.app.models.** { *; }
-keep class com.goinghatway.app.api.responses.** { *; }

# Keep Room
-keep class androidx.room.** { *; }

# Keep app models (needed for JSON deserialization)
-keepclassmembers class com.goinghatway.app.models.** {
    <fields>;
    <init>(...);
}
