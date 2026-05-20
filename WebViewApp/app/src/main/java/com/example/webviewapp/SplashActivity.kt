package com.example.webviewapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.webviewapp.data.SessionManager
import com.example.webviewapp.ui.navigation.Routes

/**
 * Punto de entrada (launcher). Implementa la "splash logic":
 *
 *  - Si hay token guardado en EncryptedSharedPreferences -> MainActivity con WEBVIEW
 *  - Si NO hay token                                     -> MainActivity con LOGIN
 *
 * Tras la migración a single-Activity + Navigation Compose, esta Activity solo
 * decide el destino inicial del NavHost y se lo pasa a MainActivity como extra.
 * Sigue usando el SplashScreen API oficial de Android 12+ para evitar parpadeos.
 */
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Debe llamarse ANTES de super.onCreate()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(applicationContext)
        val startDestination = if (sessionManager.hasToken()) Routes.WEBVIEW else Routes.LOGIN

        startActivity(
            Intent(this, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_START_DESTINATION, startDestination)
                // Limpia el stack para que el botón "atrás" no traiga al splash
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }
}
