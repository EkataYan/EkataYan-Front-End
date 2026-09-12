package com.ekatayan.app.data.di

import android.content.Context
import androidx.room.Room
import com.ekatayan.app.data.local.database.BusinessPartnerDao
import com.ekatayan.app.data.local.database.EkataYanDatabase
import com.ekatayan.app.data.local.database.GroupHubDao
import com.ekatayan.app.data.local.database.TripsDao
import com.ekatayan.app.data.local.database.WishlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalPersistenceModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EkataYanDatabase =
        Room.databaseBuilder(context, EkataYanDatabase::class.java, "ekatayan-local.db").build()

    @Provides fun provideWishlistDao(database: EkataYanDatabase): WishlistDao = database.wishlistDao()
    @Provides fun provideTripsDao(database: EkataYanDatabase): TripsDao = database.tripsDao()
    @Provides fun provideGroupHubDao(database: EkataYanDatabase): GroupHubDao = database.groupHubDao()
    @Provides fun provideBusinessPartnerDao(database: EkataYanDatabase): BusinessPartnerDao = database.businessPartnerDao()
}
