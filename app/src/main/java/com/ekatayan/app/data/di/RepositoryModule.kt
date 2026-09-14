package com.ekatayan.app.data.di

import com.ekatayan.app.data.repository.DefaultHomeRepository
import com.ekatayan.app.data.repository.DefaultNotificationsRepository
import com.ekatayan.app.data.repository.HomeRepository
import com.ekatayan.app.data.repository.NotificationsRepository
import com.ekatayan.app.data.repository.DocumentRepository
import com.ekatayan.app.data.repository.LocalDocumentRepository
import com.ekatayan.app.data.repository.AuthRepository
import com.ekatayan.app.data.repository.SupabaseAuthRepository
import com.ekatayan.app.data.remote.EncryptedSessionStore
import com.ekatayan.app.data.remote.SessionStore
import com.ekatayan.app.data.local.ProfileCacheStore
import com.ekatayan.app.data.local.SharedPreferencesProfileCacheStore
import com.ekatayan.app.data.local.AndroidProfileImageStore
import com.ekatayan.app.data.local.ProfileImageStore
import com.ekatayan.app.data.auth.AndroidGoogleCredentialProvider
import com.ekatayan.app.data.auth.GoogleCredentialProvider
import com.ekatayan.app.data.remote.realtime.NotificationRealtimeClient
import com.ekatayan.app.data.remote.realtime.OkHttpNotificationRealtimeClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSessionStore(impl: EncryptedSessionStore): SessionStore

    @Binds
    @Singleton
    abstract fun bindProfileCacheStore(impl: SharedPreferencesProfileCacheStore): ProfileCacheStore

    @Binds
    @Singleton
    abstract fun bindProfileImageStore(impl: AndroidProfileImageStore): ProfileImageStore

    @Binds
    @Singleton
    abstract fun bindGoogleCredentialProvider(impl: AndroidGoogleCredentialProvider): GoogleCredentialProvider

    @Binds
    @Singleton
    abstract fun bindNotificationRealtimeClient(
        impl: OkHttpNotificationRealtimeClient,
    ): NotificationRealtimeClient

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: SupabaseAuthRepository): AuthRepository
    @Binds
    abstract fun bindDocumentRepository(impl: LocalDocumentRepository): DocumentRepository


    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(
        impl: DefaultNotificationsRepository,
    ): NotificationsRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        impl: DefaultHomeRepository,
    ): HomeRepository
}
