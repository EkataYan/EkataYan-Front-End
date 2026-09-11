package com.ekatayan.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ekatayan.app.R
import com.ekatayan.app.data.local.database.EkataYanDatabase
import com.ekatayan.app.data.local.database.businessPartnerState
import com.ekatayan.app.data.local.database.groupHubData
import com.ekatayan.app.data.local.database.toEntities
import com.ekatayan.app.data.local.database.toEntity
import com.ekatayan.app.data.local.database.toModel
import com.ekatayan.app.data.local.database.toSnapshot
import com.ekatayan.app.data.local.database.wishlistData
import com.ekatayan.app.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalPersistenceRestartTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun structuredFrontendStateSurvivesDatabaseReopenAndDeletedSeedDoesNotReturn() = runBlocking {
        context.deleteDatabase(DATABASE_NAME)
        var database = openDatabase()
        try {
            val wishlist = WishlistData(
                groups = listOf(WishlistGroup(42, "Japan", WishlistCover.FromDevice("content://cover/42"),
                    listOf(com.ekatayan.app.data.local.WishlistDestinationCatalog.destinations.first()))),
                availableDestinations = com.ekatayan.app.data.local.WishlistDestinationCatalog.destinations,
            )
            val wishlistEntities = wishlist.toEntities()
            database.wishlistDao().initialize(wishlistEntities.first, wishlistEntities.second)
            database.wishlistDao().replace(wishlistEntities.first, wishlistEntities.second)

            val trips = listOf(Trip(77, 0, 0, R.string.trip_status_upcoming, LocalDate.parse("2026-09-11"),
                LocalDate.parse("2026-09-15"), R.drawable.galle, "Coast", "Galle", "12000", "Local trip"))
            database.tripsDao().initialize(trips.map(Trip::toEntity))
            database.tripsDao().replace(trips.map(Trip::toEntity))

            val groupData = GroupHubData(
                groups = listOf(ChatGroup("local-group", "Local Group", memberIds = listOf(CURRENT_USER_ID), theme = ChatTheme.Mint)),
                users = listOf(ChatUser(CURRENT_USER_ID, "Current User", GroupRole.Owner)),
                messagesByGroup = mapOf("local-group" to listOf(ChatMessage("local-message", "local-group", CURRENT_USER_ID,
                    MessageType.Text, "Still here", LocalDateTime.parse("2026-09-11T10:00:00"), reactions = listOf("heart")))),
            )
            database.groupHubDao().initialize(groupData.toSnapshot())
            database.groupHubDao().replace(groupData.toSnapshot())

            val partner = BusinessPartnerState(
                profile = BusinessPartnerProfile(businessName = "Local Hotel", businessType = BusinessType.HOTEL,
                    businessImageUris = listOf("content://business/photo"), submitted = true),
                listings = listOf(BusinessListing("local-listing", "Garden Room", ListingCategory.ROOMS,
                    "Quiet", 9500.0, "night", imageUris = listOf("content://listing/photo"))),
                loggedIn = true,
                demoLoaded = true,
            )
            database.businessPartnerDao().replace(partner.toSnapshot())
            database.close()

            database = openDatabase()
            val restoredWishlist = wishlistData(database.wishlistDao().observeGroups().first(), database.wishlistDao().observeItems().first())
            assertEquals("Japan", restoredWishlist.groups.single().name)
            assertEquals("content://cover/42", (restoredWishlist.groups.single().cover as WishlistCover.FromDevice).uri)
            assertEquals(listOf(1), restoredWishlist.groups.single().items.map(WishlistItem::id))
            assertEquals("Coast", database.tripsDao().observeAll().first().single().toModel().customName)

            val restoredGroup = groupHubData(database.groupHubDao().observeUsers().first(), database.groupHubDao().observeGroups().first(),
                database.groupHubDao().observeMembers().first(), database.groupHubDao().observeMessages().first(),
                database.groupHubDao().observeReactions().first())
            assertEquals(ChatTheme.Mint, restoredGroup.groups.single().theme)
            assertEquals("Still here", restoredGroup.messagesByGroup.getValue("local-group").single().text)
            assertEquals(listOf("heart"), restoredGroup.messagesByGroup.getValue("local-group").single().reactions)

            val partnerDao = database.businessPartnerDao()
            val restoredPartner = businessPartnerState(partnerDao.observeProfiles().first(), partnerDao.observeImages().first(),
                partnerDao.observeHours().first(), partnerDao.observeLinks().first(), partnerDao.observeDocuments().first(),
                partnerDao.observeListings().first(), partnerDao.observeListingImages().first(), partnerDao.observeListingFields().first(),
                partnerDao.observeBookings().first(), partnerDao.observeSession().first())
            assertEquals("Local Hotel", restoredPartner.profile.businessName)
            assertEquals("Garden Room", restoredPartner.listings.single().title)

            database.wishlistDao().replace(emptyList(), emptyList())
            database.close()
            database = openDatabase()
            database.wishlistDao().initialize(wishlistEntities.first, wishlistEntities.second)
            assertTrue(database.wishlistDao().observeGroups().first().isEmpty())
        } finally {
            if (database.isOpen) database.close()
            context.deleteDatabase(DATABASE_NAME)
        }
    }

    private fun openDatabase() = Room.databaseBuilder(context, EkataYanDatabase::class.java, DATABASE_NAME).build()

    private companion object { const val DATABASE_NAME = "local-persistence-restart-test.db" }
}
