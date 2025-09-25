# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile



-keepattributes *Annotation, InnerClasses

-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }

-keep class kotlinx.coroutines.android.** {*;}
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory {*;}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keep class com.instagram.video.downloader.data.remote.api.dto.** { *; }
-keep class com.instagram.video.downloader.data.remote.instaApi.dto.** { *; }
-keep class com.instagram.video.downloader.data.remote.utils.** { *; }

-keepattributes *Annotation
-keep class com.google.gson.examples.android.model.** { *; }

-keep,includedescriptorclasses class com.instagram.video.downloader.**$$serializer { *; }

-keepattributes Exceptions
-keep class com.google.gson.examples.android.model.** { *; }

-keep class androidx.lifecycle.LiveData { *; }


-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

-keep class tv.danmaku.ijk.media.player.** { *;}

-keep public class com.google.android.gms.** { public protected *; }

-keep class org.videolan.libvlc.** { *; }
-keep interface org.videolan.libvlc.** { *; }
-dontwarn org.videolan.libvlc.**


-dontwarn android.media.LoudnessCodecController$OnLoudnessCodecUpdateListener
-dontwarn android.media.LoudnessCodecController
-dontwarn com.android.org.conscrypt.SSLParametersImpl
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE