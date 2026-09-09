package com.ekatayan.app.data.remote

import com.ekatayan.app.BuildConfig
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

class SupabaseConfigurationException : IOException("Supabase is not configured")

class SupabaseAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val key = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        if (key.isBlank()) throw SupabaseConfigurationException()
        return chain.proceed(chain.request().newBuilder().header("apikey", key).build())
    }
}
