package com.example.webviewapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.webviewapp.ui.login.LoginScreen
import com.example.webviewapp.ui.theme.WebViewAppTheme

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebViewAppTheme {
                LoginScreen(
                    onLoginSuccess = {
                        // Después de login exitoso, abrir el WebView y cerrar el login
                        startActivity(
                            Intent(this, MainActivity::class.java).apply {
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
}
