plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.webviewapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.webviewapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Producción: estas URLs son placeholders, cámbialas cuando despliegues.
            buildConfigField("String", "API_BASE_URL", "\"https://api.sportshausen.cl/api/\"")
            buildConfigField("String", "WEBVIEW_URL", "\"https://sportshausen.cl\"")
        }
        debug {
            isMinifyEnabled = false
            // Emulador de Android Studio: 10.0.2.2 es el host de la PC.
            // Backend Express corre en :8000 (con prefijo /api/),
            // frontend Vite corre en :3000.
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/api/\"")
            buildConfigField("String", "WEBVIEW_URL", "\"http://10.0.2.2:3000\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.navigation.compose)

    // Compose (gestionado por BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    debugImplementation(libs.androidx.ui.tooling)

    // Seguridad: EncryptedSharedPreferences
    implementation(libs.androidx.security.crypto)

    // WebView mejorado (API moderna sobre CookieManager y configuración)
    implementation(libs.androidx.webkit)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
