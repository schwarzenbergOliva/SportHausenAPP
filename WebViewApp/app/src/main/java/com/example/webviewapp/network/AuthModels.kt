package com.example.webviewapp.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val role: String = "luchador"
)

/**
 * Envelope que envuelve todas las respuestas del backend SportHausen.
 * Formato: { success, data: {...}, message, error }
 */
@Serializable
data class AuthEnvelope(
    val success: Boolean = false,
    val data: AuthData? = null,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class AuthData(
    val authToken: String,
    val user: User? = null
)

/**
 * Datos del usuario tal como vienen del backend.
 * Notar que el backend usa `nombre_artistico` en signup pero solo `id/email/role`
 * en login. Por eso todos los campos son opcionales.
 */
@Serializable
data class User(
    val id: Long? = null,
    val email: String? = null,
    val role: String? = null,
    val nombre_artistico: String? = null
)
