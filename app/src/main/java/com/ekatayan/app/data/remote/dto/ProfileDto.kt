package com.ekatayan.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(val success: Boolean, val data: T?)

data class ProfilePictureUploadDto(
    val profile: ProfileDto?,
    val upload: StoredProfileImageDto?,
)

data class StoredProfileImageDto(
    val bucket: String?,
    val path: String?,
)

data class UpdateProfileRequest(
    @SerializedName("display_name") val displayName: String? = null,
    val bio: String? = null,
    @SerializedName("home_city") val homeCity: String? = null,
    val language: String? = null,
    val interests: List<String>? = null,
    val phone: String? = null,
)

data class ProfileDto(
    val id: String?,
    @SerializedName("display_name") val displayName: String?,
    val bio: String?,
    @SerializedName("home_city") val homeCity: String?,
    val language: String?,
    val interests: List<String>?,
    @SerializedName("avatar_path") val avatarPath: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("updated_at") val updatedAt: String?,
    val email: String? = null,
    val phone: String? = null,
)
