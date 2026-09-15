package com.ekatayan.app.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class WishlistDao {
    @Query("SELECT * FROM wishlist_groups ORDER BY createdOrder") abstract fun observeGroups(): Flow<List<WishlistGroupEntity>>
    @Query("SELECT * FROM wishlist_group_items ORDER BY groupId, itemOrder") abstract fun observeItems(): Flow<List<WishlistGroupItemEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM local_seed WHERE feature = 'wishlist')") abstract suspend fun isInitialized(): Boolean
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertSeed(seed: LocalSeedEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertGroups(values: List<WishlistGroupEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertItems(values: List<WishlistGroupItemEntity>)
    @Query("DELETE FROM wishlist_groups") abstract suspend fun deleteGroups()

    @Transaction
    open suspend fun initialize(groups: List<WishlistGroupEntity>, items: List<WishlistGroupItemEntity>) {
        if (!isInitialized()) {
            insertGroups(groups)
            insertItems(items)
            insertSeed(LocalSeedEntity("wishlist"))
        }
    }

    @Transaction
    open suspend fun replace(groups: List<WishlistGroupEntity>, items: List<WishlistGroupItemEntity>) {
        deleteGroups()
        insertGroups(groups)
        insertItems(items)
    }

    @Transaction
    open suspend fun clearAccountData() = deleteGroups()
}

@Dao
abstract class TripsDao {
    @Query("SELECT * FROM trips ORDER BY startDate, id") abstract fun observeAll(): Flow<List<TripEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM local_seed WHERE feature = 'trips')") abstract suspend fun isInitialized(): Boolean
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertSeed(seed: LocalSeedEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertAll(values: List<TripEntity>)
    @Query("DELETE FROM trips") abstract suspend fun deleteAll()

    @Transaction
    open suspend fun initialize(values: List<TripEntity>) {
        if (!isInitialized()) {
            insertAll(values)
            insertSeed(LocalSeedEntity("trips"))
        }
    }

    @Transaction
    open suspend fun replace(values: List<TripEntity>) {
        deleteAll()
        insertAll(values)
    }

    @Transaction
    open suspend fun clearAccountData() = deleteAll()
}

@Dao
abstract class GroupHubDao {
    @Query("SELECT * FROM chat_users ORDER BY id") abstract fun observeUsers(): Flow<List<ChatUserEntity>>
    @Query("SELECT * FROM chat_groups ORDER BY groupOrder") abstract fun observeGroups(): Flow<List<ChatGroupEntity>>
    @Query("SELECT * FROM chat_group_members ORDER BY groupId, memberOrder") abstract fun observeMembers(): Flow<List<ChatGroupMemberEntity>>
    @Query("SELECT * FROM chat_messages ORDER BY groupId, messageOrder") abstract fun observeMessages(): Flow<List<ChatMessageEntity>>
    @Query("SELECT * FROM chat_reactions ORDER BY messageId, reaction") abstract fun observeReactions(): Flow<List<ChatReactionEntity>>
    @Query("SELECT EXISTS(SELECT 1 FROM local_seed WHERE feature = 'group_hub')") abstract suspend fun isInitialized(): Boolean
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertSeed(seed: LocalSeedEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertUsers(values: List<ChatUserEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertGroups(values: List<ChatGroupEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertMembers(values: List<ChatGroupMemberEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertMessages(values: List<ChatMessageEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertReactions(values: List<ChatReactionEntity>)
    @Query("DELETE FROM chat_users") abstract suspend fun deleteUsers()
    @Query("DELETE FROM chat_groups") abstract suspend fun deleteGroups()

    @Transaction
    open suspend fun initialize(snapshot: GroupHubSnapshot) {
        if (!isInitialized()) {
            insertSnapshot(snapshot)
            insertSeed(LocalSeedEntity("group_hub"))
        }
    }

    @Transaction
    open suspend fun replace(snapshot: GroupHubSnapshot) {
        deleteGroups()
        deleteUsers()
        insertSnapshot(snapshot)
    }

    @Transaction
    open suspend fun clearAccountData() {
        deleteGroups()
        deleteUsers()
    }

    private suspend fun insertSnapshot(snapshot: GroupHubSnapshot) {
        insertUsers(snapshot.users)
        insertGroups(snapshot.groups)
        insertMembers(snapshot.members)
        insertMessages(snapshot.messages)
        insertReactions(snapshot.reactions)
    }
}

data class GroupHubSnapshot(
    val users: List<ChatUserEntity>,
    val groups: List<ChatGroupEntity>,
    val members: List<ChatGroupMemberEntity>,
    val messages: List<ChatMessageEntity>,
    val reactions: List<ChatReactionEntity>,
)

@Dao
abstract class BusinessPartnerDao {
    @Query("SELECT * FROM business_profiles") abstract fun observeProfiles(): Flow<List<BusinessProfileEntity>>
    @Query("SELECT * FROM business_images ORDER BY imageOrder") abstract fun observeImages(): Flow<List<BusinessImageEntity>>
    @Query("SELECT * FROM business_hours ORDER BY day") abstract fun observeHours(): Flow<List<BusinessHoursEntity>>
    @Query("SELECT * FROM business_social_links ORDER BY platform") abstract fun observeLinks(): Flow<List<BusinessSocialLinkEntity>>
    @Query("SELECT * FROM business_documents ORDER BY type") abstract fun observeDocuments(): Flow<List<BusinessDocumentEntity>>
    @Query("SELECT * FROM business_listings ORDER BY listingOrder") abstract fun observeListings(): Flow<List<BusinessListingEntity>>
    @Query("SELECT * FROM business_listing_images ORDER BY listingId, imageOrder") abstract fun observeListingImages(): Flow<List<BusinessListingImageEntity>>
    @Query("SELECT * FROM business_listing_fields ORDER BY listingId, field") abstract fun observeListingFields(): Flow<List<BusinessListingFieldEntity>>
    @Query("SELECT * FROM partner_bookings ORDER BY bookingOrder") abstract fun observeBookings(): Flow<List<PartnerBookingEntity>>
    @Query("SELECT * FROM business_partner_session WHERE id = 1") abstract fun observeSession(): Flow<BusinessPartnerSessionEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertProfiles(values: List<BusinessProfileEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertImages(values: List<BusinessImageEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertHours(values: List<BusinessHoursEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertLinks(values: List<BusinessSocialLinkEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertDocuments(values: List<BusinessDocumentEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertListings(values: List<BusinessListingEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertListingImages(values: List<BusinessListingImageEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertListingFields(values: List<BusinessListingFieldEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertBookings(values: List<PartnerBookingEntity>)
    @Insert(onConflict = OnConflictStrategy.REPLACE) abstract suspend fun insertSession(value: BusinessPartnerSessionEntity)
    @Query("DELETE FROM business_profiles") abstract suspend fun deleteProfiles()
    @Query("DELETE FROM business_documents") abstract suspend fun deleteDocuments()
    @Query("DELETE FROM business_listings") abstract suspend fun deleteListings()
    @Query("DELETE FROM partner_bookings") abstract suspend fun deleteBookings()
    @Query("DELETE FROM business_partner_session") abstract suspend fun deleteSession()

    @Transaction
    open suspend fun replace(snapshot: BusinessPartnerSnapshot) {
        deleteProfiles()
        deleteDocuments()
        deleteListings()
        deleteBookings()
        deleteSession()
        insertProfiles(snapshot.profiles)
        insertImages(snapshot.images)
        insertHours(snapshot.hours)
        insertLinks(snapshot.links)
        insertDocuments(snapshot.documents)
        insertListings(snapshot.listings)
        insertListingImages(snapshot.listingImages)
        insertListingFields(snapshot.listingFields)
        insertBookings(snapshot.bookings)
        insertSession(snapshot.session)
    }
}

data class BusinessPartnerSnapshot(
    val profiles: List<BusinessProfileEntity>,
    val images: List<BusinessImageEntity>,
    val hours: List<BusinessHoursEntity>,
    val links: List<BusinessSocialLinkEntity>,
    val documents: List<BusinessDocumentEntity>,
    val listings: List<BusinessListingEntity>,
    val listingImages: List<BusinessListingImageEntity>,
    val listingFields: List<BusinessListingFieldEntity>,
    val bookings: List<PartnerBookingEntity>,
    val session: BusinessPartnerSessionEntity,
)
