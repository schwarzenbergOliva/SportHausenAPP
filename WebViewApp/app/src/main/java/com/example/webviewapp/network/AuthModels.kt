package com.example.webviewapp.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Respuesta del backend SportHausen (Express → Xano). El campo del token es
 * `authToken` y opcionalmente puede traer datos del usuario.
 */
@Serializable
data class LoginResponse(
    val authToken: String,
    val user: User? = null
)

@Serializable
data class SignupRequest(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class User(
    val id: Long? = null,
    val name: String? = null,
    val email: String? = null
)
