# Conservar nombres de modelos serializados con kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.example.webviewapp.**$$serializer { *; }
-keepclassmembers class com.example.webviewapp.** {
    *** Companion;
}
-keepclasseswithmembers class com.example.webviewapp.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Retrofit (necesario para reflection sobre interfaces)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# WebView JavaScript interface (si se usara addJavascriptInterface en el futuro)
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
