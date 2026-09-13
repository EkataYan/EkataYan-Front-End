package com.ekatayan.app.data.di

import com.ekatayan.app.BuildConfig
import com.ekatayan.app.data.remote.ProfileAuthInterceptor
import com.ekatayan.app.data.remote.api.ProfileApiService
import com.ekatayan.app.data.remote.api.EkataYanApiService
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
    private fun backendRetrofit(auth: ProfileAuthInterceptor): Retrofit {
        val url = BuildConfig.BACKEND_BASE_URL.toHttpUrl()
        require(url.username.isEmpty() && url.password.isEmpty() && url.query == null && url.fragment == null)
        require(BuildConfig.DEBUG || url.isHttps) { "Release backend must use HTTPS" }
        val client = OkHttpClient.Builder().addInterceptor(auth).followRedirects(false)
            .followSslRedirects(false).callTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS).build()
        return Retrofit.Builder().baseUrl(url).client(client)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @Provides
    @Singleton
    fun profileApi(auth: ProfileAuthInterceptor): ProfileApiService {
        return backendRetrofit(auth).create(ProfileApiService::class.java)
    }

    @Provides @Singleton
    fun applicationApi(auth: ProfileAuthInterceptor): EkataYanApiService = backendRetrofit(auth).create(EkataYanApiService::class.java)
}
