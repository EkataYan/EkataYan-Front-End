package com.ekatayan.app.data.local.database

import com.ekatayan.app.data.local.WishlistDestinationCatalog
import com.ekatayan.app.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime

fun WishlistData.toEntities(): Pair<List<WishlistGroupEntity>, List<WishlistGroupItemEntity>> {
    val groups = groups.mapIndexed { index, group ->
        val (type, reference) = when (val cover = group.cover) {
            WishlistCover.None -> "NONE" to null
            is WishlistCover.FromPlace -> "PLACE" to cover.placeId.toString()
            is WishlistCover.FromDevice -> "DEVICE" to cover.uri
        }
        WishlistGroupEntity(group.id, group.name, type, reference, index.toLong())
    }
    val items = this.groups.flatMap { group ->
        group.items.mapIndexed { index, item -> WishlistGroupItemEntity(group.id, item.id, index) }
    }
    return groups to items
}

fun wishlistData(groups: List<WishlistGroupEntity>, items: List<WishlistGroupItemEntity>): WishlistData {
    val destinations = WishlistDestinationCatalog.destinations
    val byId = destinations.associateBy { it.id }
    return WishlistData(
        groups = groups.map { entity ->
            WishlistGroup(
                id = entity.id,
                name = entity.name,
                cover = when (entity.coverType) {
                    "PLACE" -> entity.coverReference?.toIntOrNull()?.let(WishlistCover::FromPlace) ?: WishlistCover.None
                    "DEVICE" -> entity.coverReference?.let(WishlistCover::FromDevice) ?: WishlistCover.None
                    else -> WishlistCover.None
                },
                items = items.filter { it.groupId == entity.id }.sortedBy { it.itemOrder }.mapNotNull { byId[it.destinationId] },
            )
        },
        availableDestinations = destinations,
    )
}

fun Trip.toEntity() = TripEntity(id, nameRes, locationRes, statusRes, startDate.toString(), endDate.toString(), imageRes,
    customName, customLocation, budget, notes, imageUri)
fun TripEntity.toModel() = Trip(id, nameRes, locationRes, statusRes, LocalDate.parse(startDate), LocalDate.parse(endDate), imageRes,
    customName, customLocation, budget, notes, imageUri)

fun GroupHubData.toSnapshot(): GroupHubSnapshot {
    val groupEntities = groups.mapIndexed { index, group -> ChatGroupEntity(group.id, group.name, group.description, group.imageRes,
        group.imageUri, group.ownerId, group.isFavourite, group.unreadCount, group.theme.name, group.backgroundUri, index) }
    val members = groups.flatMap { group -> group.memberIds.mapIndexed { index, id -> ChatGroupMemberEntity(group.id, id, index) } }
    val messages = groups.flatMap { group -> messagesByGroup[group.id].orEmpty().mapIndexed { index, message ->
        ChatMessageEntity(message.id, message.groupId, message.senderId, message.type.name, message.text, message.timestamp.toString(),
            message.attachmentUri, message.attachmentName, message.placeId, message.replyToMessageId, message.voiceDurationSeconds, index)
    } }
    val reactions = messagesByGroup.values.flatten().flatMap { message -> message.reactions.distinct().map { ChatReactionEntity(message.id, it) } }
    return GroupHubSnapshot(
        users.map { ChatUserEntity(it.id, it.name, it.role.name) }, groupEntities, members, messages, reactions,
    )
}

fun groupHubData(
    users: List<ChatUserEntity>,
    groups: List<ChatGroupEntity>,
    members: List<ChatGroupMemberEntity>,
    messages: List<ChatMessageEntity>,
    reactions: List<ChatReactionEntity>,
): GroupHubData {
    val reactionMap = reactions.groupBy(ChatReactionEntity::messageId).mapValues { (_, values) -> values.map(ChatReactionEntity::reaction) }
    val models = messages.map { entity -> ChatMessage(entity.id, entity.groupId, entity.senderId,
        enumValueOrDefault(entity.type, MessageType.Text), entity.text, runCatching { LocalDateTime.parse(entity.timestamp) }.getOrDefault(LocalDateTime.MIN),
        entity.attachmentUri, entity.attachmentName, entity.placeId, entity.replyToMessageId, reactionMap[entity.id].orEmpty(), entity.voiceDurationSeconds) }
    return GroupHubData(
        groups = groups.map { entity -> ChatGroup(entity.id, entity.name, entity.description, entity.imageRes, entity.imageUri,
            members.filter { it.groupId == entity.id }.sortedBy { it.memberOrder }.map { it.userId }, entity.ownerId,
            entity.isFavourite, entity.unreadCount, enumValueOrDefault(entity.theme, ChatTheme.DefaultBlue), entity.backgroundUri) },
        users = users.map { ChatUser(it.id, it.name, enumValueOrDefault(it.role, GroupRole.Member)) },
        messagesByGroup = groups.associate { group -> group.id to models.filter { it.groupId == group.id }.sortedBy { it.timestamp } },
    )
}

fun BusinessPartnerState.toSnapshot(): BusinessPartnerSnapshot {
    val profileEntity = BusinessProfileEntity(profile.id, profile.businessType?.name, profile.businessName, profile.email, profile.phone,
        profile.address, profile.locationLabel, profile.description, profile.imageUri, profile.submitted, profile.city, profile.district,
        profile.country, profile.ownerName, profile.contactEmail, profile.contactPhone)
    return BusinessPartnerSnapshot(
        profiles = listOf(profileEntity),
        images = profile.businessImageUris.mapIndexed { index, uri -> BusinessImageEntity(profile.id, uri, index) },
        hours = profile.businessHours.map { BusinessHoursEntity(profile.id, it.day.name, it.open, it.openingMinutes, it.closingMinutes) },
        links = profile.socialLinks.map { BusinessSocialLinkEntity(profile.id, it.platform.name, it.url) },
        documents = documents.map { (type, value) -> BusinessDocumentEntity(type.name, value.uri, value.filename) },
        listings = listings.mapIndexed { index, listing -> BusinessListingEntity(listing.id, listing.title, listing.category.name,
            listing.description, listing.price, listing.priceUnit, listing.status.name, listing.location, listing.imageRes, listing.details,
            listing.views, listing.inquiries, listing.availability.available, listing.availability.from, listing.availability.until,
            listing.availability.timeSlots, index) },
        listingImages = listings.flatMap { listing -> listing.imageUris.mapIndexed { index, uri -> BusinessListingImageEntity(listing.id, uri, index) } },
        listingFields = listings.flatMap { listing -> listing.serviceFields.map { (field, value) -> BusinessListingFieldEntity(listing.id, field.name, value) } },
        bookings = bookings.mapIndexed { index, booking -> PartnerBookingEntity(booking.id, booking.travellerName, booking.listingName,
            booking.dateLabel, booking.amount, booking.message, booking.status.name, booking.bookedOn, booking.quantity, index) },
        session = BusinessPartnerSessionEntity(loginEmail = loginEmail, loggedIn = loggedIn, demoLoaded = demoLoaded),
    )
}

fun businessPartnerState(
    profiles: List<BusinessProfileEntity>, images: List<BusinessImageEntity>, hours: List<BusinessHoursEntity>,
    links: List<BusinessSocialLinkEntity>, documents: List<BusinessDocumentEntity>, listings: List<BusinessListingEntity>,
    listingImages: List<BusinessListingImageEntity>, listingFields: List<BusinessListingFieldEntity>, bookings: List<PartnerBookingEntity>,
    session: BusinessPartnerSessionEntity?,
): BusinessPartnerState {
    val entity = profiles.firstOrNull() ?: BusinessProfileEntity("local-business", null, "", "", "", "", "", "", null,
        false, "", "", "", "", "", "")
    val profile = BusinessPartnerProfile(entity.id, entity.businessType?.let { enumValueOrNull<BusinessType>(it) }, entity.businessName,
        entity.email, entity.phone, entity.address, entity.locationLabel, entity.description, entity.imageUri,
        images.filter { it.profileId == entity.id }.sortedBy { it.imageOrder }.map { it.uri },
        hours.takeIf { it.isNotEmpty() }?.map { BusinessHours(enumValueOrDefault(it.day, BusinessDay.MONDAY), it.open, it.openingMinutes, it.closingMinutes) }
            ?: BusinessDay.entries.map { BusinessHours(it) },
        links.map { SocialLink(enumValueOrDefault(it.platform, SocialPlatform.WEBSITE), it.url) }, entity.submitted, entity.city,
        entity.district, entity.country, entity.ownerName, entity.contactEmail, entity.contactPhone)
    return BusinessPartnerState(
        profile = profile,
        documents = documents.mapNotNull { value -> enumValueOrNull<DocumentType>(value.type)?.let { it to BusinessDocument(value.uri, value.filename) } }.toMap(),
        listings = listings.map { value -> BusinessListing(value.id, value.title, enumValueOrDefault(value.category, ListingCategory.OTHER),
            value.description, value.price, value.priceUnit, enumValueOrDefault(value.status, ListingStatus.DRAFT), value.location,
            listingImages.filter { it.listingId == value.id }.sortedBy { it.imageOrder }.map { it.uri }, value.imageRes, value.details,
            value.views, value.inquiries, listingFields.filter { it.listingId == value.id }.mapNotNull { field ->
                enumValueOrNull<ListingField>(field.field)?.let { it to field.value }
            }.toMap(), ListingAvailability(value.available, value.availableFrom, value.availableUntil, value.timeSlots)) },
        bookings = bookings.map { PartnerBooking(it.id, it.travellerName, it.listingName, it.dateLabel, it.amount, it.message,
            enumValueOrDefault(it.status, BookingStatus.PENDING), it.bookedOn, it.quantity) },
        loginEmail = session?.loginEmail.orEmpty(), loggedIn = session?.loggedIn == true, demoLoaded = session?.demoLoaded == true,
    )
}

private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? = enumValues<T>().firstOrNull { it.name == value }
private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T = enumValueOrNull<T>(value) ?: default
