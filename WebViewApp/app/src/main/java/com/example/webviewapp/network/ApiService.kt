package com.example.webviewapp.network

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Endpoints del backend Node/Express. La BASE_URL se define en RetrofitClient
 * a partir de BuildConfig.BACKEND_BASE_URL.
 */
interface ApiService {

    /** Login nativo: mismo endpoint que usa la web. */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginEnvelope
}
