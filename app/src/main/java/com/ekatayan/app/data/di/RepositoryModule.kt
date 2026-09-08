package com.ekatayan.app.data.di

import com.ekatayan.app.data.repository.DefaultHomeRepository
import com.ekatayan.app.data.repository.DefaultNotificationsRepository
import com.ekatayan.app.data.repository.HomeRepository
import com.ekatayan.app.data.repository.NotificationsRepository
import com.ekatayan.app.data.repository.DocumentRepository
import com.ekatayan.app.data.repository.LocalDocumentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
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
