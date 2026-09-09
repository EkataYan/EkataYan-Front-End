package com.ekatayan.app.data.remote.api

import com.ekatayan.app.data.remote.dto.PasswordSignInRequest
import com.ekatayan.app.data.remote.dto.PasswordSignUpRequest
import com.ekatayan.app.data.remote.dto.SupabaseSessionDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthApiService {
    @POST("auth/v1/signup")
    suspend fun signUpWithPassword(
        @Body request: PasswordSignUpRequest,
    ): SupabaseSessionDto

    @POST("auth/v1/token")
    suspend fun signInWithPassword(
        @Query("grant_type") grantType: String = "password",
        @Body request: PasswordSignInRequest,
    ): SupabaseSessionDto

    @POST("auth/v1/token")
    suspend fun refreshSession(
        @Query("grant_type") grantType: String = "refresh_token",
        @Body request: RefreshTokenRequest,
    ): SupabaseSessionDto
}

data class RefreshTokenRequest(val refresh_token: String)
