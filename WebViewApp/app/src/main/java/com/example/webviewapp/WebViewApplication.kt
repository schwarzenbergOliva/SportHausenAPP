package com.example.webviewapp

import android.app.Application
import android.webkit.CookieManager
import com.example.webviewapp.data.SessionManager
import com.example.webviewapp.network.RetrofitClient

class WebViewApplication : Application() {

    /**
     * SessionManager singleton a nivel de Application: usado por el interceptor
     * de Retrofit y por las pantallas para evitar instanciar Tink / Keystore
     * varias veces (cada instancia abre el AndroidKeystore, lo cual no es gratis).
     */
    val sessionManager: SessionManager by lazy { SessionManager(this) }

    override fun onCreate() {
        super.onCreate()

        CookieManager.getInstance().setAcceptCookie(true)

        // Conectar el interceptor de Retrofit con la sesión: cada request leerá
        // el token actual de EncryptedSharedPreferences.
        RetrofitClient.tokenProvider = { sessionManager.getToken() }
    }
}
