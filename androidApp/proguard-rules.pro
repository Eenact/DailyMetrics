# Room generates the database implementation that is instantiated reflectively on Android.
-keep class * extends androidx.room3.RoomDatabase { <init>(); }
