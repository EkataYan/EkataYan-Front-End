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
    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Remove only the exact legacy Figma seeds; preserve user-created and remote data.
            db.execSQL("DELETE FROM trips WHERE remoteId IS NULL AND customName IS NULL AND id IN (1, 2, 3, 4)")
            db.execSQL("DELETE FROM wishlist_groups WHERE (id = 1 AND name = 'My Favs') OR (id = 2 AND name = 'Beach Vibes') OR (id = 3 AND name = 'Hilly Vibes')")
            db.execSQL("DELETE FROM chat_groups WHERE id IN ('fam-outings', 'work-trip', 'baddies', 'kawadahari', 'yanawa-yanawa')")
            db.execSQL("DELETE FROM chat_users WHERE id IN ('current-user', 'ashley', 'dan', 'juniper', 'peter', 'sarah', 'tim', 'yamal', 'jennie', 'kasun')")
            db.execSQL("DELETE FROM partner_bookings WHERE id IN ('booking-1', 'booking-2', 'booking-3')")
            db.execSQL("DELETE FROM business_listings WHERE id IN ('room', 'safari', 'rental')")
            db.execSQL("UPDATE business_partner_session SET demoLoaded = 0")
        }
    }
    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE wishlist_groups ADD COLUMN remoteId TEXT")
            db.execSQL("ALTER TABLE wishlist_group_items ADD COLUMN savedPlaceId TEXT")
        }
    }
    private val migration4To5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE trips ADD COLUMN canDelete INTEGER NOT NULL DEFAULT 0")
        }
    }
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EkataYanDatabase =
        Room.databaseBuilder(context, EkataYanDatabase::class.java, "ekatayan-local.db")
            .addMigrations(migration1To2, migration2To3, migration3To4, migration4To5)
            .build()

    @Provides fun provideWishlistDao(database: EkataYanDatabase): WishlistDao = database.wishlistDao()
    @Provides fun provideTripsDao(database: EkataYanDatabase): TripsDao = database.tripsDao()
    @Provides fun provideGroupHubDao(database: EkataYanDatabase): GroupHubDao = database.groupHubDao()
    @Provides fun provideBusinessPartnerDao(database: EkataYanDatabase): BusinessPartnerDao = database.businessPartnerDao()
}
