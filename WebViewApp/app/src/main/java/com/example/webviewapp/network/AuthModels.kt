package com.example.webviewapp.network

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Modelos de la API del backend Node/Express (que hace de proxy ante Xano).
 *
 * POST /api/auth/login -> {
 *   "success": true,
 *   "data": { "authToken": "<jwt>", "user": { id, email, role, full_name?, ... } },
 *   "message": "Login exitoso"
 * }
 *
 * El objeto `user` se modela como JsonObject crudo porque debemos inyectarlo
 * TAL CUAL en localStorage['user'] del WebView (la SPA React lo lee así).
 */

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginEnvelope(
    val success: Boolean = false,
    val data: LoginData? = null,
    val message: String? = null,
    val error: String? = null
)

@Serializable
data class LoginData(
    val authToken: String? = null,
    val user: JsonObject? = null
)

/**
 * Sesión ya normalizada lista para entregar al WebView. Reproduce exactamente
 * las 4 claves que el AuthContext de la web guarda en localStorage.
 */
data class SessionData(
    val authToken: String,
    val userJson: String,   // JSON.stringify(user) con el role normalizado
    val userType: String,   // role: luchador | booker | agrupacion
    val userId: String
)
