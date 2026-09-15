package com.ekatayan.app.data.local

import com.ekatayan.app.data.model.CURRENT_USER_ID
import com.ekatayan.app.data.model.ChatUser
import com.ekatayan.app.data.model.GroupRole

/** Small frontend-only people directory used by Group Hub member and sharing flows. */
object GroupHubPeopleCatalog {
    val currentUser = ChatUser(CURRENT_USER_ID, "You", GroupRole.Owner)
    val people = listOf(
        ChatUser("nethmi", "Nethmi Perera"),
        ChatUser("kasun", "Kasun Silva"),
        ChatUser("amaya", "Amaya Fernando"),
        ChatUser("dilshan", "Dilshan Jayawardena"),
    )
    val users = listOf(currentUser) + people
}
