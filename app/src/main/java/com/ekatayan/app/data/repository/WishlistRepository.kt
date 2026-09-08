package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.initialWishlistData
import com.ekatayan.app.data.model.WishlistData
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-memory data with the same host ViewModel lifetime as before. */
class WishlistRepository @Inject constructor() {
    private val mutableState = MutableStateFlow(initialWishlistData())
    val state = mutableState.asStateFlow()
    fun update(transform: (WishlistData) -> WishlistData) = mutableState.update(transform)
}
