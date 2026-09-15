package com.ekatayan.app.data.model

import androidx.annotation.DrawableRes

data class WishlistItem(
    val id: Int,
    val name: String,
    val description: String,
    val location: String? = null,
    @param:DrawableRes val imageRes: Int,
    val itemType: WishlistItemType = WishlistItemType.DESTINATION,
    val parentDestinationId: String? = null,
    val savedPlaceId: String? = null,
)

enum class WishlistItemType {
    DESTINATION,
    ATTRACTION,
}

sealed interface WishlistCover {
    data object None : WishlistCover
    data class FromPlace(val placeId: Int) : WishlistCover
    data class FromDevice(val uri: String) : WishlistCover
}

data class WishlistGroup(
    val id: Int,
    val name: String,
    val cover: WishlistCover = WishlistCover.None,
    val items: List<WishlistItem> = emptyList(),
    val remoteId: String? = null,
)

data class WishlistData(
    val groups: List<WishlistGroup>,
    val availableDestinations: List<WishlistItem>,
)
