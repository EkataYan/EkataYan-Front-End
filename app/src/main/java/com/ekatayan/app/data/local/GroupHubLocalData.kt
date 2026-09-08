package com.ekatayan.app.data.local

import com.ekatayan.app.data.model.*
import com.ekatayan.app.R
import java.time.LocalDateTime

fun initialGroupData(): GroupHubData {
    val users = listOf(
        ChatUser(CURRENT_USER_ID, "Current User", GroupRole.Owner), ChatUser("ashley", "Ashley"), ChatUser("dan", "Dan"),
        ChatUser("juniper", "Juniper"), ChatUser("peter", "Peter"), ChatUser("sarah", "Sarah"), ChatUser("tim", "Tim"),
        ChatUser("yamal", "Yamal"), ChatUser("jennie", "Jennie"), ChatUser("kasun", "Kasun"),
    )
    val groups = listOf(
        ChatGroup("fam-outings", "Fam Outings", "Weekend adventures with the family", R.drawable.hiking, memberIds = users.map(ChatUser::id), unreadCount = 3, isFavourite = true),
        ChatGroup("work-trip", "Work Trip", "Colombo client visit", R.drawable.colombo, memberIds = listOf(CURRENT_USER_ID, "ashley", "dan", "kasun"), unreadCount = 1),
        ChatGroup("baddies", "Baddies", "Sunsets and spontaneous trips", R.drawable.mirissa, memberIds = listOf(CURRENT_USER_ID, "sarah", "jennie"), isFavourite = true),
        ChatGroup("kawadahari", "Kawadahari", memberIds = listOf(CURRENT_USER_ID, "peter", "kasun")),
        ChatGroup("yanawa-yanawa", "Yanawa Yanawa", memberIds = listOf(CURRENT_USER_ID, "tim", "yamal"), unreadCount = 2),
    )
    fun msg(id: String, group: String, sender: String, text: String, minutesAgo: Long) = ChatMessage(id, group, sender, MessageType.Text, text, LocalDateTime.now().minusMinutes(minutesAgo))
    val messages = mapOf(
        "fam-outings" to listOf(msg("f1", "fam-outings", CURRENT_USER_ID, "I wanna go hiking this time for sure", 25), msg("f2", "fam-outings", "ashley", "OMG!! Really", 18), msg("f3", "fam-outings", "tim", "Well, I wanna go to the beach", 8)),
        "work-trip" to listOf(msg("w1", "work-trip", "dan", "The client meeting is at 10 AM", 55), msg("w2", "work-trip", CURRENT_USER_ID, "I’ll share the itinerary tonight", 41)),
        "baddies" to listOf(msg("b1", "baddies", "jennie", "Mirissa this weekend? 🌊", 80), msg("b2", "baddies", "sarah", "I’m in!", 70)),
        "kawadahari" to listOf(msg("k1", "kawadahari", "peter", "Kawadahari yamu!", 140)),
        "yanawa-yanawa" to listOf(msg("y1", "yanawa-yanawa", "yamal", "Train tickets are booked", 200)),
    )
    return GroupHubData(groups, users, messages, mapOf("fam-outings" to setOf("ashley")))
}
