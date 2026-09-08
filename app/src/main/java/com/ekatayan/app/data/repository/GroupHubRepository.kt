package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.initialGroupData
import com.ekatayan.app.data.model.GroupHubData
import com.ekatayan.app.data.local.WishlistDestinationCatalog
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Owned by the host ViewModel, retaining the existing session lifetime. */
class GroupHubRepository @Inject constructor() {
    private val mutableState = MutableStateFlow(initialGroupData())
    val state = mutableState.asStateFlow()
    val destinations = WishlistDestinationCatalog.destinations
    fun update(transform: (GroupHubData) -> GroupHubData) = mutableState.update(transform)
}
