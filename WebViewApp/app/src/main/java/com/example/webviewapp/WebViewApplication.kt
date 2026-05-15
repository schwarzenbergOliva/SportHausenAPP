package com.example.webviewapp

import android.app.Application
import android.webkit.CookieManager

class WebViewApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Habilitar cookies a nivel de aplicación. Es seguro hacerlo aquí
        // aunque luego se vuelva a confirmar dentro del WebView.
        CookieManager.getInstance().setAcceptCookie(true)
    }
}
