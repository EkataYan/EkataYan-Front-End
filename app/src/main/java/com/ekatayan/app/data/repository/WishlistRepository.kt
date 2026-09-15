package com.ekatayan.app.data.repository

import com.ekatayan.app.R
import com.ekatayan.app.data.local.WishlistDestinationCatalog
import com.ekatayan.app.data.local.database.WishlistDao
import com.ekatayan.app.data.local.database.toEntities
import com.ekatayan.app.data.local.database.wishlistData
import com.ekatayan.app.data.model.WishlistCover
import com.ekatayan.app.data.model.WishlistData
import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.data.model.WishlistItem
import com.ekatayan.app.data.remote.api.ApiEnvelope
import com.ekatayan.app.data.remote.api.EkataYanApiService
import com.ekatayan.app.data.remote.api.SavedPlaceDto
import com.ekatayan.app.data.remote.api.SavedPlaceRequest
import com.ekatayan.app.data.remote.api.WishlistDto
import com.ekatayan.app.data.remote.api.WishlistPatchRequest
import com.ekatayan.app.data.remote.api.WishlistRequest
import com.ekatayan.app.data.remote.apiCall
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Supabase-backed wishlist source of truth with Room used only as a local read cache. */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class WishlistRepository private constructor(
    private val dao: WishlistDao?,
    private val api: EkataYanApiService,
    testOnly: Unit?,
) {
    @Inject constructor(dao: WishlistDao, api: EkataYanApiService) : this(dao, api, null)
    internal constructor(api: EkataYanApiService) : this(null, api, Unit)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val mutableState = MutableStateFlow(WishlistData(emptyList(), WishlistDestinationCatalog.destinations))
    val state = mutableState.asStateFlow()
    private val mutationMutex = Mutex()

    init {
        if (dao != null) scope.launch {
            dao.initialize(emptyList(), emptyList())
            combine(dao.observeGroups(), dao.observeItems(), ::wishlistData).collect { mutableState.value = it }
        }
    }

    suspend fun refresh() = mutationMutex.withLock {
        replace(apiCall("Wishlists could not be loaded.") { api.wishlists() }.requireData("Wishlists could not be loaded.").map(::toDomain))
    }

    suspend fun create(name: String, initialItem: WishlistItem? = null) = mutationMutex.withLock {
        val created = apiCall("The wishlist could not be created.") { api.createWishlist(WishlistRequest(name)) }.requireData("The wishlist could not be created.")
        var group = toDomain(created)
        if (initialItem != null) {
            val place = apiCall("The wishlist was created, but the place could not be added.") { api.addWishlistPlace(created.id, initialItem.toRequest()) }
                .requireData("The wishlist was created, but the place could not be added.")
            group = group.copy(items = listOf(initialItem.copy(savedPlaceId = place.id)))
        }
        replace(mutableState.value.groups.filterNot { it.remoteId == created.id } + group)
    }

    suspend fun rename(groupId: Int, name: String) = mutationMutex.withLock {
        val group = remoteGroup(groupId)
        apiCall("The wishlist could not be renamed.") { api.updateWishlist(requireNotNull(group.remoteId), WishlistPatchRequest(name = name)) }
            .requireData("The wishlist could not be renamed.")
        replace(mutableState.value.groups.map { if (it.id == groupId) it.copy(name = name) else it })
    }

    suspend fun delete(groupId: Int) = mutationMutex.withLock {
        val group = remoteGroup(groupId)
        val response = apiCall("The wishlist could not be deleted.") { api.deleteWishlist(requireNotNull(group.remoteId)) }
        if (!response.success || response.data?.get("deleted") != true) {
            error(response.error?.message ?: "The wishlist could not be deleted.")
        }
        replace(mutableState.value.groups.filterNot { it.id == groupId })
    }

    suspend fun setPlaceCover(groupId: Int, placeId: Int) = mutationMutex.withLock {
        val group = remoteGroup(groupId)
        require(group.items.any { it.id == placeId }) { "Choose a place already saved in this wishlist." }
        apiCall("The wishlist cover could not be updated.") { api.updateWishlist(requireNotNull(group.remoteId), WishlistPatchRequest(coverPath = "catalog:$placeId")) }
            .requireData("The wishlist cover could not be updated.")
        replace(mutableState.value.groups.map {
            if (it.id == groupId) it.copy(cover = WishlistCover.FromPlace(placeId)) else it
        })
    }

    suspend fun addPlace(groupId: Int, item: WishlistItem) = mutationMutex.withLock {
        val group = remoteGroup(groupId)
        require(group.items.none { it.id == item.id }) { "This place is already in the wishlist." }
        val saved = apiCall("The place could not be saved.") { api.addWishlistPlace(requireNotNull(group.remoteId), item.toRequest()) }
            .requireData("The place could not be saved.")
        replace(mutableState.value.groups.map {
            if (it.id == groupId) it.copy(items = it.items + item.copy(savedPlaceId = saved.id)) else it
        })
    }

    suspend fun removePlace(groupId: Int, itemId: Int) = mutationMutex.withLock {
        val group = remoteGroup(groupId)
        val item = group.items.firstOrNull { it.id == itemId } ?: error("Saved place not found.")
        val savedPlaceId = item.savedPlaceId ?: error("Refresh the wishlist before removing this place.")
        val response = apiCall("The place could not be removed.") { api.removeWishlistPlace(requireNotNull(group.remoteId), savedPlaceId) }
        if (!response.success || response.data?.get("deleted") != true) {
            error(response.error?.message ?: "The place could not be removed.")
        }
        replace(mutableState.value.groups.map {
            if (it.id == groupId) it.copy(
                items = it.items.filterNot { place -> place.id == itemId },
                cover = if ((it.cover as? WishlistCover.FromPlace)?.placeId == itemId) WishlistCover.None else it.cover,
            ) else it
        })
    }

    private fun remoteGroup(groupId: Int) = mutableState.value.groups.firstOrNull { it.id == groupId && it.remoteId != null }
        ?: error("Refresh the wishlist and try again.")

    private suspend fun replace(groups: List<WishlistGroup>) {
        val next = WishlistData(groups, WishlistDestinationCatalog.destinations)
        dao?.let { storage ->
            val entities = next.toEntities()
            storage.replace(entities.first, entities.second)
        }
        mutableState.value = next
    }

    private fun toDomain(dto: WishlistDto): WishlistGroup {
        val items = dto.savedPlaces.orEmpty().map(::toDomain)
        val cover = dto.coverPath?.removePrefix("catalog:")?.toIntOrNull()
            ?.takeIf { id -> items.any { it.id == id } }
            ?.let(WishlistCover::FromPlace) ?: WishlistCover.None
        return WishlistGroup(stableId(dto.id), dto.name, cover, items, dto.id)
    }

    private fun toDomain(dto: SavedPlaceDto): WishlistItem {
        val catalogId = dto.externalPlaceId?.removePrefix("catalog:")?.toIntOrNull()
        return WishlistDestinationCatalog.destinations.firstOrNull { it.id == catalogId }
            ?.copy(savedPlaceId = dto.id)
            ?: WishlistItem(
                id = stableId(dto.id),
                name = dto.name,
                description = dto.description,
                location = dto.location.takeIf(String::isNotBlank),
                imageRes = R.drawable.home_header,
                savedPlaceId = dto.id,
            )
    }

    private fun WishlistItem.toRequest() = SavedPlaceRequest(
        externalPlaceId = "catalog:$id",
        name = name,
        location = location.orEmpty(),
        description = description,
    )

    private fun stableId(remoteId: String): Int =
        (runCatching { UUID.fromString(remoteId).hashCode() }.getOrElse { remoteId.hashCode() } and Int.MAX_VALUE)
            .takeUnless { it == 0 } ?: 1

    private fun <T> ApiEnvelope<T>.requireData(fallback: String): T =
        data.takeIf { success } ?: error(error?.message ?: fallback)
}
