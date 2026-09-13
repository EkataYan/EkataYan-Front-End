package com.ekatayan.app.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE trips ADD COLUMN remoteId TEXT")
            db.execSQL("ALTER TABLE trips ADD COLUMN source TEXT NOT NULL DEFAULT 'manual'")
            db.execSQL("ALTER TABLE trips ADD COLUMN summary TEXT")
            db.execSQL("ALTER TABLE trips ADD COLUMN route TEXT")
            db.execSQL("ALTER TABLE trips ADD COLUMN travellerType TEXT")
            db.execSQL("ALTER TABLE trips ADD COLUMN travellerCount INTEGER")
            db.execSQL("ALTER TABLE trips ADD COLUMN travelStyle TEXT")
            db.execSQL("ALTER TABLE trips ADD COLUMN travelPace TEXT")
        }
    }
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EkataYanDatabase =
        Room.databaseBuilder(context, EkataYanDatabase::class.java, "ekatayan-local.db").addMigrations(migration1To2).build()

    @Provides fun provideWishlistDao(database: EkataYanDatabase): WishlistDao = database.wishlistDao()
    @Provides fun provideTripsDao(database: EkataYanDatabase): TripsDao = database.tripsDao()
    @Provides fun provideGroupHubDao(database: EkataYanDatabase): GroupHubDao = database.groupHubDao()
    @Provides fun provideBusinessPartnerDao(database: EkataYanDatabase): BusinessPartnerDao = database.businessPartnerDao()
}
