package com.ekatayan.app.data.remote.api

import com.ekatayan.app.data.remote.dto.ApiResponse
import com.ekatayan.app.data.remote.dto.ProfileDto
import retrofit2.http.GET

interface ProfileApiService {
    @GET("api/users/me")
    suspend fun getProfile(): ApiResponse<ProfileDto>?
}
