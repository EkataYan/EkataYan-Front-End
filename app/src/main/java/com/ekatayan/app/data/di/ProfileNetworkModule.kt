package com.ekatayan.app.data.di

import com.ekatayan.app.BuildConfig
import com.ekatayan.app.data.remote.ProfileAuthInterceptor
import com.ekatayan.app.data.remote.api.ProfileApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object ProfileNetworkModule {
    @Provides
    @Singleton
    fun profileApi(auth: ProfileAuthInterceptor): ProfileApiService {
        val url = BuildConfig.BACKEND_BASE_URL.toHttpUrl()
        require(url.username.isEmpty() && url.password.isEmpty() && url.query == null && url.fragment == null)
        require(BuildConfig.DEBUG || url.isHttps) { "Release backend must use HTTPS" }
        val client = OkHttpClient.Builder()
            .addInterceptor(auth)
            .followRedirects(false)
            .followSslRedirects(false)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder().baseUrl(url).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(ProfileApiService::class.java)
    }
}
