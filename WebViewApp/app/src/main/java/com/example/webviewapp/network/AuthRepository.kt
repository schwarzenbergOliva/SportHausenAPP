package com.example.webviewapp.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull

/**
 * Repositorio de autenticación contra el backend Node/Express.
 *
 * Replica la lógica del AuthContext de la web: tras el login, normaliza el
 * `role` y produce una [SessionData] con las 4 claves que la SPA espera en
 * localStorage (authToken, user, userType, userId).
 */
class AuthRepository(
    private val api: ApiService = RetrofitClient.api
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun login(email: String, password: String): Result<SessionData> = runCatching {
        val env = api.login(LoginRequest(email = email.trim(), password = password))

        val token = env.data?.authToken
        if (!env.success || token.isNullOrBlank()) {
            error(env.message ?: env.error ?: "Credenciales inválidas")
        }

        val user = env.data?.user ?: buildJsonObject { }

        // Normalizar role igual que el frontend
        val rawRole = user.str("role") ?: user.str("tipo_usuario") ?: user.str("type") ?: "luchador"
        val role = when {
            rawRole == "agrupación" -> "agrupacion"
            rawRole in VALID_ROLES -> rawRole
            else -> "luchador"
        }

        val userId = user.str("id") ?: user.str("user_id") ?: ""

        // Reconstruir el objeto user con el role normalizado y serializarlo
        val normalizedUser = JsonObject(
            user.toMutableMap().apply { put("role", JsonPrimitive(role)) }
        )
        val userJson = json.encodeToString(JsonObject.serializer(), normalizedUser)

        SessionData(
            authToken = token,
            userJson = userJson,
            userType = role,
            userId = userId
        )
    }

    private fun JsonObject.str(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() && it != "null" }

    companion object {
        private val VALID_ROLES = setOf("booker", "agrupacion", "luchador")
    }
}
