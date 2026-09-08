package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.data.model.WishlistItem

data class WishlistUiState(
    val groups: List<WishlistGroup> = emptyList(),
    val availableDestinations: List<WishlistItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
