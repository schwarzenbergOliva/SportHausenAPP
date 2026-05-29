package com.example.webviewapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Endpoints del backend SportHausen (wrapper Express → Xano).
 *
 * BASE_URL ya incluye el prefijo `/api/`, por eso los paths aquí son
 * relativos a `auth/...`.
 *
 * Token: el AuthInterceptor adjunta automáticamente
 * `Authorization: Bearer <authToken>` cuando hay sesión.
 */
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): LoginResponse

    @POST("auth/logout")
    suspend fun logout()

    @GET("auth/me")
    suspend fun me(): User
}
