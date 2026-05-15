package com.example.webviewapp.network

import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Repositorio de autenticación.
 *
 * En modo simulado valida credenciales en memoria y genera un token UUID.
 * Cuando integres tu backend real, cambia `USE_FAKE_AUTH` a false y la
 * llamada delegará en ApiService a través de Retrofit.
 */
class AuthRepository(
    private val api: ApiService = RetrofitClient.api
) {

    suspend fun login(email: String, password: String): Result<String> = runCatching {
        if (USE_FAKE_AUTH) {
            // Simulación de latencia de red
            delay(800)
            require(email.isNotBlank() && password.length >= 6) {
                "Credenciales inválidas"
            }
            // En un caso real, el token vendría del backend
            "fake-token-${UUID.randomUUID()}"
        } else {
            val response = api.login(LoginRequest(email = email, password = password))
            response.token
        }
    }

    companion object {
        // Cámbialo a false cuando tengas la API REST lista
        private const val USE_FAKE_AUTH = true
    }
}
