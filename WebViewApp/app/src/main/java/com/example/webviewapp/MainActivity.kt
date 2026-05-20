package com.example.webviewapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.webviewapp.data.SessionManager
import com.example.webviewapp.ui.navigation.AppNavHost
import com.example.webviewapp.ui.navigation.Routes
import com.example.webviewapp.ui.theme.WebViewAppTheme

class MainActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = intent.getStringExtra(EXTRA_START_DESTINATION)
            ?: if (sessionManager.hasToken()) Routes.WEBVIEW else Routes.LOGIN

        setContent {
            WebViewAppTheme {
                AppNavHost(
                    sessionManager = sessionManager,
                    targetUrl = TARGET_URL,
                    startDestination = startDestination
                )
            }
        }
    }

    companion object {
        const val EXTRA_START_DESTINATION = "extra_start_destination"
        private const val TARGET_URL = "https://tu-sitio-web.com"
    }
}
