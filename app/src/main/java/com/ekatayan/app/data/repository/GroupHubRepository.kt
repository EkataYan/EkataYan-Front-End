package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.WishlistDestinationCatalog
import com.ekatayan.app.data.local.database.GroupHubDao
import com.ekatayan.app.data.local.database.groupHubData
import com.ekatayan.app.data.local.database.toSnapshot
import com.ekatayan.app.data.model.GroupHubData
import com.ekatayan.app.data.model.ChatUser
import com.ekatayan.app.data.model.GroupRole
import com.ekatayan.app.data.model.CURRENT_USER_ID
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

/** Local-only Room repository; typing indicators intentionally remain ephemeral. */
@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GroupHubRepository private constructor(private val dao: GroupHubDao?, testMode: Boolean) {
    @Inject constructor(dao: GroupHubDao) : this(dao, false)
    constructor() : this(null, true)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val mutableState = MutableStateFlow(GroupHubData())
    val state = mutableState.asStateFlow()
    val destinations = WishlistDestinationCatalog.destinations
    // Do not surface the old Figma people catalogue as real app users.
    val directoryUsers = listOf(ChatUser(CURRENT_USER_ID, "You", GroupRole.Admin))

    init {
        if (dao != null) scope.launch {
            dao.initialize(GroupHubData().toSnapshot())
            combine(dao.observeUsers(), dao.observeGroups(), dao.observeMembers(), dao.observeMessages(), dao.observeReactions(),
                ::groupHubData).collect { mutableState.value = it }
        }
    }

    fun update(transform: (GroupHubData) -> GroupHubData) {
        val updated = synchronized(this) { transform(mutableState.value).also { mutableState.value = it } }
        dao?.let { storage -> scope.launch { runCatching { storage.replace(updated.toSnapshot()) } } }
    }
}
