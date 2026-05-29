package com.example.webviewapp.network

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Endpoints del backend SportHausen (wrapper Express → Xano).
 *
 * BASE_URL ya incluye el prefijo `/api/`, por eso los paths aquí son
 * relativos a `auth/...`.
 *
 * Token: el AuthInterceptor adjunta automáticamente
 * `Authorization: Bearer <authToken>` cuando hay sesión.
 *
 * Todas las respuestas vienen envueltas en `{ success, data, message, error }`.
 */
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthEnvelope

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): AuthEnvelope

    @POST("auth/logout")
    suspend fun logout(): GenericEnvelope
}

/** Envelope para endpoints que no devuelven data tipada (logout). */
@Serializable
data class GenericEnvelope(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
