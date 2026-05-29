package com.example.webviewapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.webviewapp.ui.navigation.AppNavHost
import com.example.webviewapp.ui.navigation.Routes
import com.example.webviewapp.ui.theme.WebViewAppTheme
import com.example.webviewapp.network.AuthRepository
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val app get() = application as WebViewApplication
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionManager = app.sessionManager
        val startDestination = intent.getStringExtra(EXTRA_START_DESTINATION)
            ?: if (sessionManager.hasToken()) Routes.WEBVIEW else Routes.LOGIN

        setContent {
            WebViewAppTheme {
                AppNavHost(
                    sessionManager = sessionManager,
                    targetUrl = BuildConfig.WEBVIEW_URL,
                    startDestination = startDestination,
                    onLogoutRequested = { performLogout() }
                )
            }
        }
    }

    /**
     * Logout en 3 pasos:
     *  1. Avisar al backend (best-effort, no bloquea si falla).
     *  2. Borrar el token local (siempre, aunque el backend haya fallado).
     *  3. La navegación al login la dispara el NavHost vía callback.
     */
    private fun performLogout() {
        lifecycleScope.launch {
            authRepository.logout()
            app.sessionManager.clearSession()
        }
    }

    companion object {
        const val EXTRA_START_DESTINATION = "extra_start_destination"
    }
}
