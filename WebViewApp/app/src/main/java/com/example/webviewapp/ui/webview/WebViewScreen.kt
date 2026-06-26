package com.example.webviewapp.ui.webview

import android.annotation.SuppressLint
import android.net.Uri
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Pantalla principal (Opción B): WebView que carga la web React ya "logueada".
 *
 * El hand-off de sesión se hace inyectando las 4 claves que el AuthContext de
 * la web espera en localStorage (authToken, user, userType, userId) ANTES de
 * que arranque el JS de la SPA, usando WebViewCompat.addDocumentStartJavaScript.
 *
 * @param startUrl URL completa a cargar (p. ej. https://web/panel/luchador)
 * @param authToken JWT de sesión
 * @param userJson  JSON del usuario (se guarda en localStorage['user'])
 * @param userType  rol del usuario
 * @param userId    id del usuario
 * @param onLogout  callback cuando el usuario cierra sesión
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(
    startUrl: String,
    authToken: String,
    userJson: String,
    userType: String,
    userId: String,
    onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val uri = remember(startUrl) { Uri.parse(startUrl) }
    val allowedHost = remember(uri) { uri.host.orEmpty() }
    val origin = remember(uri) {
        buildString {
            append(uri.scheme); append("://"); append(uri.host)
            if (uri.port != -1) { append(":"); append(uri.port) }
        }
    }

    // Script que reproduce lo que AuthContext.login() guarda en localStorage.
    val bootstrapScript = remember(authToken, userJson, userType, userId) {
        buildString {
            append("(function(){try{")
            append("localStorage.setItem('authToken',").append(JSONObject.quote(authToken)).append(");")
            append("localStorage.setItem('user',").append(JSONObject.quote(userJson)).append(");")
            append("localStorage.setItem('userType',").append(JSONObject.quote(userType)).append(");")
            append("localStorage.setItem('userId',").append(JSONObject.quote(userId)).append(");")
            append("}catch(e){console.error('bootstrap',e);}})();")
        }
    }

    val supportsDocStart = remember {
        WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)
    }

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
                            domStorageEnabled = true          // localStorage: imprescindible
                            cacheMode = WebSettings.LOAD_DEFAULT
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                            allowFileAccess = false
                            allowContentAccess = false
                        }

                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        // Inyección PREVIA al arranque de la SPA (vía DocumentStart).
                        if (supportsDocStart) {
                            WebViewCompat.addDocumentStartJavaScript(
                                this,
                                bootstrapScript,
                                setOf(origin)
                            )
                        }

                        webViewClient = SecureWebViewClient(
                            allowedHost = allowedHost,
                            // Fallback de inyección si no hay DOCUMENT_START_SCRIPT.
                            startScript = if (supportsDocStart) null else bootstrapScript,
                            onPageStarted = { isLoading = true; progress = 0f },
                            onPageFinished = { isLoading = false; progress = 1f },
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
                        loadUrl(startUrl)
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
