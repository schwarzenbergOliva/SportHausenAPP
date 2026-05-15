package com.example.webviewapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.webviewapp.data.SessionManager

/**
 * Punto de entrada (launcher). Implementa la "splash logic":
 *
 *  - Si hay token guardado en EncryptedSharedPreferences -> MainActivity (WebView)
 *  - Si NO hay token                                     -> LoginActivity
 *
 * Usa el SplashScreen API oficial de Android 12+ (con backport hacia atrás)
 * para que el sistema dibuje el splash y nosotros decidamos a dónde ir antes
 * de hacer setContent. Esto evita parpadeos.
 */
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Debe llamarse ANTES de super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(applicationContext)
        val nextActivity = if (sessionManager.hasToken()) {
            MainActivity::class.java
        } else {
            LoginActivity::class.java
        }

        startActivity(Intent(this, nextActivity).apply {
            // Limpia el stack para que el botón "atrás" no traiga al splash
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }
}
