package com.example.webviewapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.webviewapp.data.SessionManager
import com.example.webviewapp.ui.theme.WebViewAppTheme
import com.example.webviewapp.ui.webview.WebViewScreen

class MainActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val token = sessionManager.getToken()

        setContent {
            WebViewAppTheme {
                WebViewScreen(
                    url = TARGET_URL,
                    authToken = token,
                    onLogout = {
                        sessionManager.clearSession()
                        startActivity(
                            Intent(this, LoginActivity::class.java).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            }
                        )
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        // URL solicitada en el enunciado
        private const val TARGET_URL = "https://tu-sitio-web.com"
    }
}
