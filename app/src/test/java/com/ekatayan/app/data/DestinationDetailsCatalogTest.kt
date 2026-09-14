package com.ekatayan.app.data

import com.ekatayan.app.data.local.DestinationDetailsCatalog
import com.ekatayan.app.data.local.WishlistDestinationCatalog
import com.ekatayan.app.data.model.WishlistItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DestinationDetailsCatalogTest {
    @Test
    fun allCurrentHomeDestinationIdsResolveToReusableDetails() {
        listOf(1, 11, 12, 14, 15).forEach { wishlistItemId ->
            val routeId = DestinationDetailsCatalog.destinationIdForWishlistItem(wishlistItemId)
            assertNotNull(routeId)
            assertNotNull(DestinationDetailsCatalog.destination(requireNotNull(routeId)))
        }
    }

    @Test
    fun polonnaruwaContainsRequestedStablePlaceIds() {
        val destination = requireNotNull(DestinationDetailsCatalog.destination("polonnaruwa"))
        assertEquals(
            listOf("gal_vihara", "royal_palace", "rankoth_vehera", "parakrama_samudra", "lankatilaka_temple"),
            DestinationDetailsCatalog.popularPlaces(destination).map { it.id },
        )
    }

    @Test
    fun attractionsArePersistableWishlistCatalogueItems() {
        val galVihara = WishlistDestinationCatalog.destinations.first { it.name == "Gal Vihara" }
        assertEquals(WishlistItemType.ATTRACTION, galVihara.itemType)
        assertEquals("polonnaruwa", galVihara.parentDestinationId)
        assertTrue(WishlistDestinationCatalog.destinations.count { it.id == galVihara.id } == 1)
    }
}
