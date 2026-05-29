package com.example.webviewapp.network

import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio de autenticación contra el backend SportHausen.
 *
 * Devuelve `Result<T>` con excepciones tipadas (AuthException) para que el
 * ViewModel pueda mostrar mensajes específicos según la causa del fallo.
 */
class AuthRepository(
    private val api: ApiService = RetrofitClient.api
) {

    suspend fun login(email: String, password: String): Result<String> = safeCall {
        val response = api.login(LoginRequest(email = email.trim(), password = password))
        require(response.authToken.isNotBlank()) { "El servidor no devolvió un token válido." }
        response.authToken
    }

    suspend fun signup(name: String, email: String, password: String): Result<String> = safeCall {
        val response = api.signup(
            SignupRequest(name = name.trim(), email = email.trim(), password = password)
        )
        require(response.authToken.isNotBlank()) { "El servidor no devolvió un token válido." }
        response.authToken
    }

    /** Cierra la sesión en el backend (best-effort). Si falla, el cliente igual borra su token. */
    suspend fun logout(): Result<Unit> = safeCall { api.logout() }

    /** Verifica que el token actual sigue siendo válido. */
    suspend fun me(): Result<User> = safeCall { api.me() }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: HttpException) {
        Result.failure(mapHttpError(e))
    } catch (e: IOException) {
        Result.failure(AuthException.NoConnection)
    } catch (e: IllegalArgumentException) {
        Result.failure(AuthException.Generic(e.message ?: "Datos inválidos"))
    } catch (e: Exception) {
        Result.failure(AuthException.Generic(e.message ?: "Error inesperado"))
    }

    private fun mapHttpError(e: HttpException): AuthException = when (e.code()) {
        400 -> AuthException.Generic("Datos inválidos")
        401, 403 -> AuthException.InvalidCredentials
        404 -> AuthException.Generic("Servicio no disponible")
        in 500..599 -> AuthException.ServerError
        else -> AuthException.Generic("Error de red (${e.code()})")
    }
}

sealed class AuthException(message: String) : Exception(message) {
    object InvalidCredentials : AuthException("Email o contraseña incorrectos")
    object NoConnection : AuthException("Sin conexión a internet")
    object ServerError : AuthException("El servidor no responde, intenta más tarde")
    data class Generic(val msg: String) : AuthException(msg)
}
