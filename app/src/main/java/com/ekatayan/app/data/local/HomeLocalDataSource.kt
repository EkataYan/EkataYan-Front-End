package com.ekatayan.app.data.local

import com.ekatayan.app.R
import com.ekatayan.app.data.model.PopularDestination
import com.ekatayan.app.data.model.RecommendedDestination
import com.ekatayan.app.data.model.UpcomingTrip
import com.ekatayan.app.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeLocalDataSource @Inject constructor() {

    fun getUser(): User = User(name = "Zendaya", profileImageRes = null)

    fun getRecommendedDestinations(): List<RecommendedDestination> = listOf(
        RecommendedDestination(1, "Polonnaruwa", "Explore Sri Lanka's majestic ancient capital and its remarkable heritage.", R.drawable.polonnaruwa),
        RecommendedDestination(2, "Nine Arch Bridge", "Walk through Ella's misty hills to this iconic railway landmark.", R.drawable.nine_arch_bridge),
        RecommendedDestination(3, "Sigiriya", "Experience one of Sri Lanka's most iconic ancient landmarks.", R.drawable.sigiriya),
    )

    fun getUpcomingTrip(): UpcomingTrip? = UpcomingTrip(1, "Anuradhapura", "26 Aug 2026", "6 Days", R.drawable.anuradhapura)

    fun getPopularDestinations(): List<PopularDestination> = listOf(
        PopularDestination(1, "Sigiriya", R.drawable.sigiriya),
        PopularDestination(2, "Nine Arch Bridge", R.drawable.nine_arch_bridge),
        PopularDestination(3, "Kandy", R.drawable.kandy),
        PopularDestination(4, "Polonnaruwa", R.drawable.polonnaruwa),
        PopularDestination(5, "Galle", R.drawable.galle),
    )
}
