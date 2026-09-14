package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.WishlistDestinationCatalog
import com.ekatayan.app.data.local.database.WishlistDao
import com.ekatayan.app.data.local.database.toEntities
import com.ekatayan.app.data.local.database.wishlistData
import com.ekatayan.app.data.model.WishlistData
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Room-backed source of truth for groups, covers and destination membership. */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class WishlistRepository private constructor(private val dao: WishlistDao?, testMode: Boolean) {
    @Inject constructor(dao: WishlistDao) : this(dao, false)
    constructor() : this(null, true)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val mutableState = MutableStateFlow(WishlistData(emptyList(), WishlistDestinationCatalog.destinations))
    val state = mutableState.asStateFlow()

    init {
        if (dao != null) scope.launch {
            dao.initialize(emptyList(), emptyList())
            combine(dao.observeGroups(), dao.observeItems(), ::wishlistData).collect { mutableState.value = it }
        }
    }

    fun update(transform: (WishlistData) -> WishlistData) {
        val updated = synchronized(this) { transform(mutableState.value).also { mutableState.value = it } }
        dao?.let { storage -> scope.launch {
            val entities = updated.toEntities()
            runCatching { storage.replace(entities.first, entities.second) }
        } }
    }
}
