package com.example.webviewapp.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Adjunta `Authorization: Bearer <token>` a cada request saliente cuando hay token.
 *
 * Recibe un proveedor (no la sesión directamente) para que el interceptor no
 * dependa de Android / Context, y para que siempre lea el token más reciente
 * desde el SessionManager sin necesidad de reconstruir el cliente.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = chain.request().newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()
        return chain.proceed(request)
    }
}
