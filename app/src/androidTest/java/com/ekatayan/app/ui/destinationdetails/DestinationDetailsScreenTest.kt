package com.ekatayan.app.ui.destinationdetails

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.ekatayan.app.core.designsystem.theme.EkataYanTheme
import com.ekatayan.app.data.local.DestinationDetailsCatalog
import com.ekatayan.app.data.local.WishlistDestinationCatalog
import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.data.model.ChatGroup
import com.ekatayan.app.viewmodel.GroupHubUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DestinationDetailsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun destinationShowsDynamicContentAndNavigatesWithStablePlaceId() {
        val destination = requireNotNull(DestinationDetailsCatalog.destination("polonnaruwa"))
        val wishlistItem = WishlistDestinationCatalog.destinations.first { it.id == destination.wishlistItemId }
        var selectedPlaceId: String? = null
        composeRule.setContent {
            EkataYanTheme(darkTheme = false) {
                DestinationDetailsScreen(
                    destination = destination,
                    popularPlaces = DestinationDetailsCatalog.popularPlaces(destination),
                    wishlistItem = wishlistItem,
                    wishlistGroups = emptyList(),
                    onBackClick = {},
                    onSharePlace = { _, _, _ -> true },
                    onPlaceClick = { selectedPlaceId = it },
                    onGroupSelectionChange = { _, _, _ -> },
                    onCreateGroupWithPlace = { _, _ -> true },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Share").assertIsDisplayed()
        composeRule.onNodeWithText("About Polonnaruwa").assertIsDisplayed()
        composeRule.onNodeWithText("Gal Vihara").performScrollTo().performClick()
        composeRule.runOnIdle { assertEquals("gal_vihara", selectedPlaceId) }
    }

    @Test
    fun wishlistSelectorShowsCurrentMembershipAndCreateAction() {
        val destination = requireNotNull(DestinationDetailsCatalog.destination("polonnaruwa"))
        val wishlistItem = WishlistDestinationCatalog.destinations.first { it.id == destination.wishlistItemId }
        val groups = listOf(WishlistGroup(7, "My Favs", items = listOf(wishlistItem)))
        composeRule.setContent {
            EkataYanTheme(darkTheme = false) {
                DestinationDetailsScreen(
                    destination = destination,
                    popularPlaces = emptyList(),
                    wishlistItem = wishlistItem,
                    wishlistGroups = groups,
                    onBackClick = {},
                    onSharePlace = { _, _, _ -> true },
                    onPlaceClick = {},
                    onGroupSelectionChange = { _, _, _ -> },
                    onCreateGroupWithPlace = { _, _ -> true },
                )
            }
        }

        composeRule.onNodeWithText("Add to Wishlist").performScrollTo().performClick()
        composeRule.onNodeWithText("Save to Wishlist").assertIsDisplayed()
        composeRule.onNodeWithText("My Favs").assertIsDisplayed()
        composeRule.onNodeWithText("Create New Wishlist").assertIsDisplayed()
    }

    @Test
    fun shareButtonOpensReusableGroupHubPickerAndSendsSelection() {
        val destination = requireNotNull(DestinationDetailsCatalog.destination("polonnaruwa"))
        val wishlistItem = WishlistDestinationCatalog.destinations.first { it.id == destination.wishlistItemId }
        var sentGroupIds = emptySet<String>()
        composeRule.setContent {
            EkataYanTheme(darkTheme = false) {
                DestinationDetailsScreen(
                    destination = destination,
                    popularPlaces = emptyList(),
                    wishlistItem = wishlistItem,
                    wishlistGroups = emptyList(),
                    groupHubState = GroupHubUiState(groups = listOf(ChatGroup("crew", "Travel Crew"))),
                    onBackClick = {},
                    onSharePlace = { groups, _, _ -> sentGroupIds = groups; true },
                    onPlaceClick = {},
                    onGroupSelectionChange = { _, _, _ -> },
                    onCreateGroupWithPlace = { _, _ -> true },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Share").performClick()
        composeRule.onNodeWithText("Share Place").assertIsDisplayed()
        composeRule.onNodeWithText("Travel Crew").performClick()
        composeRule.onNodeWithText("Send").performClick()
        composeRule.runOnIdle { assertEquals(setOf("crew"), sentGroupIds) }
    }

    @Test
    fun placeShowsDetailsSectionsAndWishlistAction() {
        val attraction = requireNotNull(DestinationDetailsCatalog.attraction("gal_vihara"))
        val wishlistItem = WishlistDestinationCatalog.destinations.first { it.id == attraction.wishlistItemId }
        composeRule.setContent {
            EkataYanTheme(darkTheme = false) {
                PlaceDetailsScreen(
                    attraction = attraction,
                    wishlistItem = wishlistItem,
                    wishlistGroups = emptyList(),
                    onBackClick = {},
                    onSharePlace = { _, _, _ -> true },
                    onGroupSelectionChange = { _, _, _ -> },
                    onCreateGroupWithPlace = { _, _ -> true },
                )
            }
        }

        composeRule.onNodeWithText("About Gal Vihara").assertIsDisplayed()
        composeRule.onNodeWithText("History Lovers").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Highlights").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Tips for Visitors").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Add to Wishlist").performScrollTo().assertIsDisplayed()
    }
}
