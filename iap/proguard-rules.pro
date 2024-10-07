-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**

# Only necessary if you downloaded the SDK jar directly instead of from maven.
-keep class com.shaded.fasterxml.jackson.** { *; }
-keepattributes LineNumberTable,SourceFile
-renamesourcefileattribute SourceFile
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-keep class com.android.vending.billing.**
# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
# some built in classes
-keep class io.codetail.animation.arcanimator.** { *; }
-keep class android.support.v7.widget.SearchView { *; }
-keep class android.widget.SearchView { *; }
-keep class androidx.appcompat.widget.SearchView { *; }
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep class android.support.v7.app.** { *; }
-keep interface android.support.v7.app.** { *; }
-keep public class * extends android.app.Application
-keep class androidx.renderscript.**{*;}
-keep class com.synnapps.carouselview.** { *; }
#-keep class yourpackage.** { *; }
#-dontoptimize
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontpreverify
-allowaccessmodification
-optimizations !code/simplification/arithmetic
-keepattributes *Annotation*
#-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
#all view classes
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class org.apache.commons.logging.**               { *; }
-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }
-keep class org.codehaus.**                             { *; }
-dontwarn javax.xml.stream.events.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.impl.**
-dontwarn org.apache.http.conn.scheme.**
-keep class com.google.gson.stream.** { *; }
-keep public class org.apache.commons.io.**
-keep public class com.google.gson.**
-keep public class com.google.gson.** {public private protected *;}
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontnote okhttp3.**
# Okio
-keep class sun.misc.Unsafe { *; }
-dontwarn java.nio.file.*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-keep public class com.google.vending.licensing.ILicensingService
-keep public class com.android.vending.licensing.ILicensingService
-keep public class com.google.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService
-dontnote com.google.vending.licensing.ILicensingService
-dontnote com.google.android.vending.licensing.ILicensingService
-keepclassmembers enum * { *; }
-keep class com.google.gson.stream.** { *; }
# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}
# Preserve some attributes that may be required for reflection.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
# Keep setters in Views so that animations can still work.
-keepclassmembers public class * extends android.view.View {
    void set*(***);
    *** get*();
}
# We want to keep methods in Activity that could be used in the XML attribute onClick.
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}
# For enumeration classes, see http://proguard.sourceforge.net/manual/examples.html#enumerations
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
# Preserve annotated Javascript interface methods.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# The support libraries contains references to newer platform versions.
# Don't warn about those in case this app is linking against an older
# platform version. We know about them, and they are safe.

-dontnote android.support.**
-dontnote androidx.**
-dontwarn android.support.**
-dontwarn androidx.**

# This class is deprecated, but remains for backward compatibility.

-dontwarn android.util.FloatMath

# Understand the @Keep support annotation.

-keep class android.support.annotation.Keep
-keep class androidx.annotation.Keep
-keep @android.support.annotation.Keep class * {*;}
-keep @androidx.annotation.Keep class * {*;}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @android.support.annotation.Keep <init>(...);
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# These classes are duplicated between android.jar and org.apache.http.legacy.jar.

-dontnote org.apache.http.**
-dontnote android.net.http.**
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Class names are needed in reflection

-keepnames class com.amazonaws.**
-keepnames class com.amazon.**
-keepclassmembers enum com.amazonaws.** { *; }

# Request handlers defined in request.handlers

-keep class com.amazonaws.services.**.*Handler
-keep class !com.dzinemedia.logomaker.** { *; }
# The following are referenced but aren't required to run
-keep class **.R
-dontwarn com.fasterxml.jackson.**
-dontwarn org.apache.commons.logging.**

# The SDK has several references of Apache HTTP client
-keep class com.android.vending.billing.**
-keep class com.aemerse.iap.IapConnector.**
-keep class com.dzinemedia.logomaker.model.** { *; }
-keep class com.dzinemedia.logomaker.undoredomanager.** { *; }
#-keep class com.dzinemedia.logomaker.layersArea.model.** { *; }
-keep class com.dzinemedia.logomaker.camerax.Option.** { *; }
-keep class com.dzinemedia.logomaker.camerax.OptionView.** { *; }
-keep class com.dzinemedia.logomaker.camerax.Option.**{*;}
-keep class com.dzinemedia.logomaker.downloadArea.DownloadingTemplateClass.**{*;}
#-keep class com.dzinemedia.logmaker.model.CustomTempModel.**{*;}
#-keep class com.dzinemedia.logomaker.model.customTempModel.deserializers.**{*;}

-keep class com.dzinemedia.logmaker.model.CustomTempModel.Adaptation.**{*;}
-keep class com.dzinemedia.logomaker.camerax.CameraActivity.**{*;}
-keep class com.dzinemedia.logomaker.camerax.MessageView.**{*;}

-dontwarn com.amazonaws.http.**
-dontwarn com.amazonaws.metrics.**
-keep class com.khrystal.library.widget.** { *; }
#helpful links, ayashi kro
#https://github.com/aws-amplify/aws-sdk-android/issues/704
#https://www.zacsweers.dev/android-proguard-rules/
#https://guides.codepath.com/android/Configuring-ProGuard
#https://stackoverflow.com/questions/15543607/assertionerror-in-gson-enumtypeadapter-when-using-proguard-obfuscation