package com.example.webviewapp.network

import retrofit2.HttpException
import java.io.IOException

/**
 * Repositorio de autenticación contra el backend SportHausen.
 *
 * Las respuestas del backend vienen con el envoltorio
 * `{ success, data: { authToken, user }, message, error }`. Aquí extraemos
 * el `authToken` y mapeamos errores HTTP a `AuthException` tipadas.
 */
class AuthRepository(
    private val api: ApiService = RetrofitClient.api
) {

    suspend fun login(email: String, password: String): Result<String> = safeCall {
        val response = api.login(LoginRequest(email = email.trim(), password = password))
        extractToken(response)
    }

    suspend fun signup(name: String, email: String, password: String): Result<String> = safeCall {
        val response = api.signup(
            SignupRequest(name = name.trim(), email = email.trim(), password = password)
        )
        extractToken(response)
    }

    /** Cierra la sesión en el backend (best-effort). Si falla, el cliente igual borra su token. */
    suspend fun logout(): Result<Unit> = safeCall { api.logout(); Unit }

    /**
     * Extrae authToken validando el envelope. Si success=false o data viene null,
     * lanza para que safeCall lo capture y mapee a un error de UI.
     */
    private fun extractToken(envelope: AuthEnvelope): String {
        if (!envelope.success || envelope.data == null) {
            throw AuthException.Generic(
                envelope.error ?: envelope.message ?: "Respuesta inválida del servidor"
            )
        }
        val token = envelope.data.authToken
        require(token.isNotBlank()) { "El servidor no devolvió un token válido." }
        return token
    }

    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: AuthException) {
        Result.failure(e)
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
