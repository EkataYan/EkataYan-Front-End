package com.ekatayan.app.data.local

import com.ekatayan.app.data.model.*

fun initialWishlistData(): WishlistData {
    val destinations = WishlistDestinationCatalog.destinations
    return WishlistData(
        availableDestinations = destinations,
        groups = listOf(
            WishlistGroup(1, "My Favs", WishlistCover.FromPlace(1), destinations.filter { it.id in 1..3 }),
            WishlistGroup(2, "Beach Vibes", WishlistCover.FromPlace(4), destinations.filter { it.id in 4..7 }),
            WishlistGroup(3, "Hilly Vibes", WishlistCover.FromPlace(9), destinations.filter { it.id in 8..11 }),
        ),
    )
}
