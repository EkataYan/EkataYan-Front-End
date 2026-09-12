package com.ekatayan.app.data.remote.api

import com.ekatayan.app.data.remote.dto.ApiResponse
import com.ekatayan.app.data.remote.dto.ProfileDto
import com.ekatayan.app.data.remote.dto.ProfilePictureUploadDto
import com.ekatayan.app.data.remote.dto.UpdateProfileRequest
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Response
import retrofit2.http.Streaming

interface ProfileApiService {
    @GET("api/users/me")
    suspend fun getProfile(): ApiResponse<ProfileDto>?

    @PATCH("api/users/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): ApiResponse<ProfileDto>?

    @Multipart
    @POST("api/storage/profile-picture")
    suspend fun uploadProfilePicture(@Part file: MultipartBody.Part): ApiResponse<ProfilePictureUploadDto>?

    @Streaming
    @GET("api/storage/profile-picture")
    suspend fun getProfilePicture(): Response<ResponseBody>
}
