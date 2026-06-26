package com.example.webviewapp

import android.content.Intent
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.webviewapp.data.SessionManager
import com.example.webviewapp.ui.theme.WebViewAppTheme
import com.example.webviewapp.ui.webview.WebViewScreen

/**
 * Pantalla principal tras el login (Opción B): carga la web React ya logueada
 * dentro de un WebView, en el dashboard que corresponde al rol del usuario.
 */
class MainActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val token = sessionManager.getToken().orEmpty()
        val userJson = sessionManager.getUserJson().orEmpty()
        val role = sessionManager.getUserType() ?: "luchador"
        val userId = sessionManager.getUserId().orEmpty()

        // Sin token válido -> volver al login.
        if (token.isBlank()) {
            goToLogin()
            return
        }

        val startUrl = BuildConfig.FRONTEND_BASE_URL.trimEnd('/') + dashboardPath(role)

        setContent {
            WebViewAppTheme {
                WebViewScreen(
                    startUrl = startUrl,
                    authToken = token,
                    userJson = userJson,
                    userType = role,
                    userId = userId,
                    onLogout = {
                        clearWebData()
                        sessionManager.clearSession()
                        goToLogin()
                    }
                )
            }
        }
    }

    /** Ruta de destino según el rol, igual que el frontend tras el login. */
    private fun dashboardPath(role: String): String = when (role) {
        "luchador" -> "/panel/luchador"
        else -> "/dashboard/$role"
    }

    private fun clearWebData() {
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
    }

    private fun goToLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
    }
}
