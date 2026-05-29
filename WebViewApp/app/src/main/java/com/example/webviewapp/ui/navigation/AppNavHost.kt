package com.example.webviewapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.webviewapp.data.SessionManager
import com.example.webviewapp.ui.login.LoginScreen
import com.example.webviewapp.ui.webview.WebViewScreen

object Routes {
    const val LOGIN = "login"
    const val WEBVIEW = "webview"
}

/**
 * Grafo de navegación de la app.
 *
 * @param onLogoutRequested side-effects que ocurren al cerrar sesión
 *   (llamar al endpoint /auth/logout, borrar el token cifrado). El NavHost se
 *   limita a la navegación; las consecuencias persistentes vienen de fuera.
 */
@Composable
fun AppNavHost(
    sessionManager: SessionManager,
    targetUrl: String,
    startDestination: String,
    onLogoutRequested: () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.WEBVIEW) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.WEBVIEW) {
            WebViewScreen(
                url = targetUrl,
                authToken = sessionManager.getToken(),
                onLogout = {
                    onLogoutRequested()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.WEBVIEW) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
