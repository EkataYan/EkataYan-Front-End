package com.ekatayan.app.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalSeedEntity::class,
        WishlistGroupEntity::class,
        WishlistGroupItemEntity::class,
        TripEntity::class,
        ChatUserEntity::class,
        ChatGroupEntity::class,
        ChatGroupMemberEntity::class,
        ChatMessageEntity::class,
        ChatReactionEntity::class,
        BusinessProfileEntity::class,
        BusinessImageEntity::class,
        BusinessHoursEntity::class,
        BusinessSocialLinkEntity::class,
        BusinessDocumentEntity::class,
        BusinessListingEntity::class,
        BusinessListingImageEntity::class,
        BusinessListingFieldEntity::class,
        PartnerBookingEntity::class,
        BusinessPartnerSessionEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class EkataYanDatabase : RoomDatabase() {
    abstract fun wishlistDao(): WishlistDao
    abstract fun tripsDao(): TripsDao
    abstract fun groupHubDao(): GroupHubDao
    abstract fun businessPartnerDao(): BusinessPartnerDao
}
