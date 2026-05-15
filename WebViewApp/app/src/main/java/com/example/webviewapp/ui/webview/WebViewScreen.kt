package com.example.webviewapp.ui.webview

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.launch

/**
 * Pantalla principal: WebView embebido en Compose vía AndroidView.
 *
 * Características:
 *  - inyección de cookies con CookieManager ANTES de loadUrl
 *  - barra de progreso lineal mientras carga la página
 *  - manejo del botón Atrás del sistema integrado con el historial del WebView
 *  - WebViewClient con manejo de errores y restricción al host permitido
 *
 * @param url URL inicial a cargar
 * @param authToken token de sesión que se inyecta como cookie
 * @param onLogout callback cuando el usuario solicita salir (token inválido, etc.)
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    authToken: String?,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }

    // Referencia al WebView para poder usar canGoBack/goBack desde BackHandler
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val allowedHost = remember(url) { url.toUri().host.orEmpty() }

    // Inyectar cookies ANTES de cargar la URL. Es CRÍTICO el orden:
    //   1. setAcceptCookie(true)
    //   2. setCookie(url, "name=value; flags")
    //   3. flush()  -> obliga a persistir antes del loadUrl
    LaunchedEffect(authToken, url) {
        if (!authToken.isNullOrBlank()) {
            CookieManager.getInstance().apply {
                setAcceptCookie(true)
                // Atributos importantes:
                //   Path=/         -> aplica a toda la web
                //   Secure         -> solo se envía por HTTPS
                //   SameSite=Lax   -> evita CSRF básico permitiendo navegación normal
                // NOTA: HttpOnly NO se puede setear desde el cliente y, si lo añades,
                // el backend igualmente lo respetará a la entrada; aquí lo omitimos.
                setCookie(
                    url,
                    "session_token=$authToken; Path=/; Secure; SameSite=Lax"
                )
                flush()
            }
        }
    }

    // Botón Atrás: navega el historial del WebView; si no hay historial, cerrar.
    BackHandler(enabled = webViewRef?.canGoBack() == true) {
        webViewRef?.goBack()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding: PaddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        // ---------- Configuración segura y funcional ----------
                        settings.apply {
                            // JavaScript: necesario para casi cualquier web moderna.
                            // Tu app es responsable de cargar solo URLs confiables.
                            javaScriptEnabled = true

                            // DOM storage (localStorage / sessionStorage)
                            domStorageEnabled = true

                            // Caché
                            cacheMode = WebSettings.LOAD_DEFAULT

                            // Viewport y zoom
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false

                            // Carga mixta solo si es estrictamente necesaria.
                            // Lo dejamos en MIXED_CONTENT_NEVER_ALLOW por defecto.
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

                            // Bloquear acceso al sistema de archivos por defecto
                            allowFileAccess = false
                            allowContentAccess = false
                            // Estos dos son deprecated y por defecto false en SDK 30+;
                            // los dejamos explícitos por seguridad.
                            @Suppress("DEPRECATION")
                            allowFileAccessFromFileURLs = false
                            @Suppress("DEPRECATION")
                            allowUniversalAccessFromFileURLs = false
                        }

                        // Habilitar cookies de terceros para este WebView (necesario
                        // si tu backend usa cookies en dominios distintos).
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        // Forzar modo oscuro si está disponible y el sistema está en dark.
                        // Usa la API moderna de WebViewCompat (androidx.webkit).
                        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                            val isDark = (resources.configuration.uiMode and
                                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                                android.content.res.Configuration.UI_MODE_NIGHT_YES
                            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isDark)
                        }

                        // ---------- WebViewClient (errores + filtro de host) ----------
                        webViewClient = SecureWebViewClient(
                            allowedHost = allowedHost,
                            onPageStarted = {
                                isLoading = true
                                progress = 0f
                            },
                            onPageFinished = {
                                isLoading = false
                                progress = 1f
                            },
                            onError = { code, description ->
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Error al cargar ($code): $description"
                                    )
                                }
                            }
                        )

                        // ---------- WebChromeClient (progreso) ----------
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                                isLoading = newProgress < 100
                            }
                        }

                        webViewRef = this
                        loadUrl(url)
                    }
                },
                onRelease = { webView ->
                    // Liberar el WebView al salir de composición para evitar memory leaks
                    webView.stopLoading()
                    webView.webChromeClient = null
                    webView.destroy()
                    webViewRef = null
                }
            )

            // Barra de progreso superior; visible solo durante la carga.
            // Esto cubre el gap entre "página descargada" y "CSS renderizado".
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
