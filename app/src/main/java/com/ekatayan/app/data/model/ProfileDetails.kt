package com.ekatayan.app.data.model

data class ProfileDetails(
    val name: String,
    val location: String,
    val email: String = "",
    val phone: String = "",
    val trips: String = "—",
    val placesVisited: String = "—",
    val averageRating: String = "—",
    val travelBuddies: String = "—",
    val bio: String = "",
    val language: String = "",
    val interests: List<String> = emptyList(),
    val avatarPath: String? = null,
    val avatarLocalPath: String? = null,
    val username: String = "",
)
