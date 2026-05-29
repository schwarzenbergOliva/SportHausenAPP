package com.example.webviewapp.network

import com.example.webviewapp.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit perezoso.
 *
 * - BASE_URL se inyecta desde BuildConfig (API_BASE_URL), así cambiar entre
 *   dev (http://10.0.2.2:8000/api/) y prod (https://...) no requiere editar
 *   código, solo cambiar de buildType / flavor.
 * - El token se adjunta vía AuthInterceptor que lee de tokenProvider en cada
 *   request. WebViewApplication lo configura al arrancar.
 */
object RetrofitClient {

    /**
     * Proveedor de token de sesión. WebViewApplication lo apunta a
     * SessionManager.getToken(). Mientras no se setee, las requests salen sin
     * Authorization (correcto para login/signup).
     */
    @Volatile
    var tokenProvider: () -> String? = { null }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AuthInterceptor { tokenProvider() })
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }
}
