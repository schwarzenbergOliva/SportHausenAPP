package com.example.webviewapp.ui.webview

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebViewClient personalizado con:
 *  - manejo de errores que expone callbacks para mostrar UI nativa
 *  - bloqueo de navegación fuera del dominio permitido (refuerza la seguridad)
 *  - callbacks de start/finish para sincronizar la barra de progreso/loader
 */
class SecureWebViewClient(
    private val allowedHost: String,
    private val onPageStarted: (WebView?) -> Unit = {},
    private val onPageFinished: () -> Unit = {},
    private val onError: (code: Int, description: String) -> Unit = { _, _ -> }
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest
    ): Boolean {
        val host = request.url.host ?: return false
        // Permite navegar dentro del dominio o subdominios; cualquier otra
        // URL la dejamos al WebView y aquí podrías delegar a Custom Tabs.
        return !host.endsWith(allowedHost)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        onPageStarted(view)
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
        // Solo nos interesan los errores del recurso principal (no de subrecursos)
        if (request.isForMainFrame) {
            onError(error.errorCode, error.description?.toString().orEmpty())
        }
    }
}
