# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK's default proguard configuration.

# Keep Room entities and DAOs
-keep class com.tichuguru.db.** { *; }
-keep class com.tichuguru.model.** { *; }
-keep class com.tichuguru.repository.** { *; }
