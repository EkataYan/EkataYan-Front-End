package com.ekatayan.app.data.remote.api

import com.ekatayan.app.data.remote.dto.ApiResponse
import com.ekatayan.app.data.remote.dto.ProfileDto
import com.ekatayan.app.data.remote.dto.UpdateProfileRequest
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PATCH

interface ProfileApiService {
    @GET("api/users/me")
    suspend fun getProfile(): ApiResponse<ProfileDto>?

    @PATCH("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<ProfileDto>?
}
