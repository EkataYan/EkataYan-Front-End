package com.ekatayan.app.data.di

import com.ekatayan.app.BuildConfig
import com.ekatayan.app.data.remote.SupabaseAuthInterceptor
import com.ekatayan.app.data.remote.api.SupabaseAuthApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {
    @Provides
    @Singleton
    fun supabaseAuthApi(): SupabaseAuthApiService {
        val url = BuildConfig.SUPABASE_URL.toHttpUrl()
        require(url.isHttps && url.username.isEmpty() && url.password.isEmpty() && url.query == null && url.fragment == null)
        return Retrofit.Builder().baseUrl(url)
            .client(OkHttpClient.Builder().addInterceptor(SupabaseAuthInterceptor())
                .followRedirects(false).followSslRedirects(false).callTimeout(30, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(SupabaseAuthApiService::class.java)
    }
}
