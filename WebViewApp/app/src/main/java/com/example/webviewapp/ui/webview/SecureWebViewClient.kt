package com.example.webviewapp.ui.webview

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebViewClient con:
 *  - restricción de navegación al host permitido (refuerza la seguridad)
 *  - inyección de un script de arranque en onPageStarted (fallback cuando el
 *    dispositivo no soporta DOCUMENT_START_SCRIPT)
 *  - callbacks de start/finish/error para la UI nativa
 */
class SecureWebViewClient(
    private val allowedHost: String,
    private val startScript: String? = null,
    private val onPageStarted: () -> Unit = {},
    private val onPageFinished: () -> Unit = {},
    private val onError: (code: Int, description: String) -> Unit = { _, _ -> }
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {
        val host = request.url.host ?: return false
        // Permitir el host permitido y sus subdominios; bloquear el resto.
        val allowed = host == allowedHost || host.endsWith(".$allowedHost")
        return !allowed
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        // Fallback de inyección: si DOCUMENT_START_SCRIPT no está disponible,
        // escribimos las claves lo antes posible (antes de que React monte).
        startScript?.let { view?.evaluateJavascript(it, null) }
        onPageStarted()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        onPageFinished()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest,
        error: WebResourceError
    ) {
        super.onReceivedError(view, request, error)
        if (request.isForMainFrame) {
            onError(error.errorCode, error.description?.toString().orEmpty())
        }
    }
}
