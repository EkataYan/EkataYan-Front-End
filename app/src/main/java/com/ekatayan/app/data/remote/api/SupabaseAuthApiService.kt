package com.ekatayan.app.data.remote.api

import com.ekatayan.app.data.remote.dto.PasswordSignInRequest
import com.ekatayan.app.data.remote.dto.PasswordSignUpRequest
import com.ekatayan.app.data.remote.dto.SupabaseSessionDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.Header

interface SupabaseAuthApiService {
    @POST("auth/v1/recover")
    suspend fun requestPasswordRecovery(
        @Body request: PasswordRecoveryRequest,
    ): Map<String, Any?>

    @PUT("auth/v1/user")
    suspend fun updatePassword(
        @Header("Authorization") authorization: String,
        @Body request: PasswordUpdateRequest,
    ): Map<String, Any?>
    @POST("auth/v1/token")
    suspend fun signInWithIdToken(
        @Query("grant_type") grantType: String = "id_token",
        @Body request: GoogleIdTokenRequest,
    ): SupabaseSessionDto

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

data class PasswordUpdateRequest(val password: String)

data class PasswordRecoveryRequest(val email: String)

data class RefreshTokenRequest(val refresh_token: String)

data class GoogleIdTokenRequest(
    val provider: String = "google",
    @com.google.gson.annotations.SerializedName("id_token") val idToken: String,
    val nonce: String? = null,
)
