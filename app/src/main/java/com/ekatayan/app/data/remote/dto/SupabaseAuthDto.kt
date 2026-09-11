package com.ekatayan.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PasswordSignInRequest(val email: String, val password: String)

data class PasswordSignUpMetadata(
    @SerializedName("full_name") val fullName: String,
    val phone: String,
)

data class SupabaseUserMetadataDto(
    @SerializedName("full_name") val fullName: String? = null,
)

data class SupabaseUserDto(
    val id: String?,
    val email: String?,
    @SerializedName("user_metadata") val userMetadata: SupabaseUserMetadataDto? = null,
)

data class PasswordSignUpRequest(
    val email: String,
    val password: String,
    val data: PasswordSignUpMetadata,
)

data class SupabaseSessionDto(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresInSeconds: Long?,
    @SerializedName("expires_at") val expiresAtSeconds: Long?,
    val user: SupabaseUserDto? = null,
)
