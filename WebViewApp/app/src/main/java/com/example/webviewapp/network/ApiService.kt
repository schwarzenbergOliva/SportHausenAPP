package com.example.webviewapp.network

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Endpoints REST. Reemplaza la URL base en RetrofitClient cuando integres
 * el backend real.
 */
interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
