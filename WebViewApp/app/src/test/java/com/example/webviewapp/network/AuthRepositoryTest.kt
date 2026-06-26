package com.example.webviewapp.network

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la lógica de [AuthRepository]: normalización de rol y manejo del
 * envelope del backend. Usa un ApiService falso (sin red).
 */
class AuthRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun repo(env: LoginEnvelope) = AuthRepository(object : ApiService {
        override suspend fun login(request: LoginRequest): LoginEnvelope = env
    })

    private fun ok(role: String?, id: Int = 7, token: String = "jwt.abc.123") =
        LoginEnvelope(
            success = true,
            data = LoginData(
                authToken = token,
                user = buildJsonObject {
                    put("id", id)
                    put("email", "user@example.com")
                    if (role != null) put("role", role)
                }
            )
        )

    @Test
    fun `acento en agrupacion se normaliza`() = runTest {
        val result = repo(ok("agrupación")).login("user@example.com", "secret6")
        assertTrue(result.isSuccess)
        val session = result.getOrThrow()
        assertEquals("agrupacion", session.userType)
        assertEquals("7", session.userId)
        // El userJson inyectado debe llevar el role normalizado
        val role = json.parseToJsonElement(session.userJson).jsonObject["role"]?.jsonPrimitive?.content
        assertEquals("agrupacion", role)
    }

    @Test
    fun `rol desconocido cae a luchador`() = runTest {
        val session = repo(ok("administrador-raro")).login("u@e.com", "secret6").getOrThrow()
        assertEquals("luchador", session.userType)
    }

    @Test
    fun `rol ausente usa luchador por defecto`() = runTest {
        val session = repo(ok(null)).login("u@e.com", "secret6").getOrThrow()
        assertEquals("luchador", session.userType)
    }

    @Test
    fun `roles validos se preservan`() = runTest {
        for (r in listOf("booker", "agrupacion", "luchador")) {
            val session = repo(ok(r)).login("u@e.com", "secret6").getOrThrow()
            assertEquals(r, session.userType)
        }
    }

    @Test
    fun `success false devuelve fallo con mensaje`() = runTest {
        val env = LoginEnvelope(success = false, message = "Credenciales inválidas")
        val result = repo(env).login("u@e.com", "x")
        assertTrue(result.isFailure)
        assertEquals("Credenciales inválidas", result.exceptionOrNull()?.message)
    }

    @Test
    fun `token en blanco devuelve fallo`() = runTest {
        val env = LoginEnvelope(success = true, data = LoginData(authToken = "", user = buildJsonObject {}))
        assertTrue(repo(env).login("u@e.com", "secret6").isFailure)
    }

    @Test
    fun `token valido devuelve exito`() = runTest {
        val session = repo(ok("luchador")).login("u@e.com", "secret6").getOrThrow()
        assertEquals("jwt.abc.123", session.authToken)
        assertFalse(session.userJson.isBlank())
    }
}
