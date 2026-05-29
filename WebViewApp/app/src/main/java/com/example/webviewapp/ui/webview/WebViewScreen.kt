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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

/**
 * Pantalla principal: WebView que carga el frontend React de SportHausen.
 *
 * Estrategia de auth: el frontend (Vite + React) lee `authToken` desde
 * `localStorage`. Inyectamos el token nativo dentro del WebView ANTES de que
 * arranque el bundle de React:
 *  - Preferido: `WebViewCompat.addDocumentStartJavaScript` (corre antes que
 *    cualquier <script> de la página). Disponible con androidx.webkit cuando
 *    DOCUMENT_START_SCRIPT está soportado por el WebView del dispositivo.
 *  - Fallback: `evaluateJavascript` en `onPageStarted`. Corre un instante
 *    después del primer parse, pero en la práctica llega antes de que React
 *    lea localStorage durante su mount.
 *
 * En logout limpiamos cookies + `localStorage` para evitar sesiones zombi.
 *
 * @param url URL del frontend a cargar
 * @param authToken token de sesión a inyectar en localStorage del WebView
 * @param onLogout callback al cerrar sesión
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    url: String,
    authToken: String?,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val allowedHost = remember(url) { url.toUri().host.orEmpty() }
    val injectScript = remember(authToken) { buildAuthInjectionScript(authToken) }

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

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            allowFileAccess = false
                            allowContentAccess = false
                            @Suppress("DEPRECATION")
                            allowFileAccessFromFileURLs = false
                            @Suppress("DEPRECATION")
                            allowUniversalAccessFromFileURLs = false
                        }

                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                            val isDark = (resources.configuration.uiMode and
                                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                                android.content.res.Configuration.UI_MODE_NIGHT_YES
                            WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, isDark)
                        }

                        // Inyección preferida del token en localStorage: corre antes que el bundle React.
                        if (injectScript != null &&
                            WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
                        ) {
                            WebViewCompat.addDocumentStartJavaScript(
                                this, injectScript, setOf("*")
                            )
                        }

                        webViewClient = SecureWebViewClient(
                            allowedHost = allowedHost,
                            onPageStarted = { view ->
                                isLoading = true
                                progress = 0f
                                // Fallback: si DOCUMENT_START_SCRIPT no estaba disponible,
                                // metemos el token apenas comienza a cargar la página.
                                if (injectScript != null &&
                                    !WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
                                ) {
                                    view?.evaluateJavascript(injectScript, null)
                                }
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
                    webView.stopLoading()
                    webView.webChromeClient = null
                    webView.destroy()
                    webViewRef = null
                }
            )

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

/**
 * Construye el script que mete el authToken en localStorage. Escapamos
 * comillas y backslashes para que un token con caracteres especiales (típico
 * de JWT) no rompa el JS inyectado.
 */
private fun buildAuthInjectionScript(token: String?): String? {
    if (token.isNullOrBlank()) return null
    val escaped = token
        .replace("\\", "\\\\")
        .replace("'", "\\'")
    return "try { localStorage.setItem('authToken', '$escaped'); } catch (e) {}"
}

/**
 * Helper invocado desde MainActivity al hacer logout. Limpia cualquier rastro
 * de sesión del WebView (cookies + localStorage). Es estático para no requerir
 * referencia a la composición.
 */
fun clearWebViewSession(webView: WebView?) {
    webView?.evaluateJavascript(
        "try { localStorage.clear(); sessionStorage.clear(); } catch (e) {}",
        null
    )
    CookieManager.getInstance().removeAllCookies(null)
    CookieManager.getInstance().flush()
}
