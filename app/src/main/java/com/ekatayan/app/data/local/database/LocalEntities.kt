package com.ekatayan.app.data.local.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "local_seed", primaryKeys = ["feature"])
data class LocalSeedEntity(val feature: String, val initialized: Boolean = true)

@Entity(tableName = "wishlist_groups", primaryKeys = ["id"])
data class WishlistGroupEntity(
    val id: Int,
    val name: String,
    val coverType: String,
    val coverReference: String?,
    val createdOrder: Long,
    val remoteId: String? = null,
)

@Entity(
    tableName = "wishlist_group_items",
    primaryKeys = ["groupId", "destinationId"],
    foreignKeys = [ForeignKey(
        entity = WishlistGroupEntity::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("groupId"), Index("destinationId")],
)
data class WishlistGroupItemEntity(val groupId: Int, val destinationId: Int, val itemOrder: Int, val savedPlaceId: String? = null)

@Entity(tableName = "trips", primaryKeys = ["id"])
data class TripEntity(
    val id: Int,
    val nameRes: Int,
    val locationRes: Int,
    val statusRes: Int,
    val startDate: String,
    val endDate: String,
    val imageRes: Int,
    val customName: String?,
    val customLocation: String?,
    val budget: String?,
    val notes: String?,
    val imageUri: String? = null,
    val remoteId: String? = null,
    val source: String = "manual",
    val summary: String? = null,
    val route: String? = null,
    val travellerType: String? = null,
    val travellerCount: Int? = null,
    val travelStyle: String? = null,
    val travelPace: String? = null,
)

@Entity(tableName = "chat_users", primaryKeys = ["id"])
data class ChatUserEntity(val id: String, val name: String, val role: String)

@Entity(tableName = "chat_groups", primaryKeys = ["id"])
data class ChatGroupEntity(
    val id: String,
    val name: String,
    val description: String,
    val imageRes: Int?,
    val imageUri: String?,
    val ownerId: String,
    val isFavourite: Boolean,
    val unreadCount: Int,
    val theme: String,
    val backgroundUri: String?,
    val groupOrder: Int,
)

@Entity(
    tableName = "chat_group_members",
    primaryKeys = ["groupId", "userId"],
    foreignKeys = [
        ForeignKey(entity = ChatGroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ChatUserEntity::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("groupId"), Index("userId")],
)
data class ChatGroupMemberEntity(val groupId: String, val userId: String, val memberOrder: Int)

@Entity(
    tableName = "chat_messages",
    primaryKeys = ["id"],
    foreignKeys = [ForeignKey(entity = ChatGroupEntity::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("groupId"), Index("replyToMessageId")],
)
data class ChatMessageEntity(
    val id: String,
    val groupId: String,
    val senderId: String,
    val type: String,
    val text: String?,
    val timestamp: String,
    val attachmentUri: String?,
    val attachmentName: String?,
    val placeId: Int?,
    val replyToMessageId: String?,
    val voiceDurationSeconds: Int?,
    val messageOrder: Int,
)

@Entity(
    tableName = "chat_reactions",
    primaryKeys = ["messageId", "reaction"],
    foreignKeys = [ForeignKey(entity = ChatMessageEntity::class, parentColumns = ["id"], childColumns = ["messageId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("messageId")],
)
data class ChatReactionEntity(val messageId: String, val reaction: String)

@Entity(tableName = "business_profiles", primaryKeys = ["id"])
data class BusinessProfileEntity(
    val id: String,
    val businessType: String?,
    val businessName: String,
    val email: String,
    val phone: String,
    val address: String,
    val locationLabel: String,
    val description: String,
    val imageUri: String?,
    val submitted: Boolean,
    val city: String,
    val district: String,
    val country: String,
    val ownerName: String,
    val contactEmail: String,
    val contactPhone: String,
)

@Entity(
    tableName = "business_images",
    primaryKeys = ["profileId", "uri"],
    foreignKeys = [ForeignKey(entity = BusinessProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("profileId")],
)
data class BusinessImageEntity(val profileId: String, val uri: String, val imageOrder: Int)

@Entity(
    tableName = "business_hours",
    primaryKeys = ["profileId", "day"],
    foreignKeys = [ForeignKey(entity = BusinessProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("profileId")],
)
data class BusinessHoursEntity(val profileId: String, val day: String, val open: Boolean, val openingMinutes: Int, val closingMinutes: Int)

@Entity(
    tableName = "business_social_links",
    primaryKeys = ["profileId", "platform"],
    foreignKeys = [ForeignKey(entity = BusinessProfileEntity::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("profileId")],
)
data class BusinessSocialLinkEntity(val profileId: String, val platform: String, val url: String)

@Entity(tableName = "business_documents", primaryKeys = ["type"])
data class BusinessDocumentEntity(val type: String, val uri: String, val filename: String)

@Entity(tableName = "business_listings", primaryKeys = ["id"])
data class BusinessListingEntity(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val price: Double,
    val priceUnit: String,
    val status: String,
    val location: String,
    val imageRes: Int?,
    val details: String,
    val views: Int,
    val inquiries: Int,
    val available: Boolean,
    val availableFrom: String,
    val availableUntil: String,
    val timeSlots: String,
    val listingOrder: Int,
)

@Entity(
    tableName = "business_listing_images",
    primaryKeys = ["listingId", "uri"],
    foreignKeys = [ForeignKey(entity = BusinessListingEntity::class, parentColumns = ["id"], childColumns = ["listingId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("listingId")],
)
data class BusinessListingImageEntity(val listingId: String, val uri: String, val imageOrder: Int)

@Entity(
    tableName = "business_listing_fields",
    primaryKeys = ["listingId", "field"],
    foreignKeys = [ForeignKey(entity = BusinessListingEntity::class, parentColumns = ["id"], childColumns = ["listingId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("listingId")],
)
data class BusinessListingFieldEntity(val listingId: String, val field: String, val value: String)

@Entity(tableName = "partner_bookings", primaryKeys = ["id"])
data class PartnerBookingEntity(
    val id: String,
    val travellerName: String,
    val listingName: String,
    val dateLabel: String,
    val amount: Double,
    val message: String,
    val status: String,
    val bookedOn: String,
    val quantity: Int,
    val bookingOrder: Int,
)

@Entity(tableName = "business_partner_session", primaryKeys = ["id"])
data class BusinessPartnerSessionEntity(
    val id: Int = 1,
    val loginEmail: String,
    val loggedIn: Boolean,
    val demoLoaded: Boolean,
)
