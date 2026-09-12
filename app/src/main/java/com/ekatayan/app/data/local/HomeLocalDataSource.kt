package com.ekatayan.app.data.local

import com.ekatayan.app.R
import com.ekatayan.app.data.model.UpcomingTrip
import com.ekatayan.app.data.model.User
import com.ekatayan.app.data.model.WeatherInfo
import com.ekatayan.app.data.model.WeatherType
import com.ekatayan.app.data.model.WishlistItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeLocalDataSource @Inject constructor() {

    fun getUser(): User = User(name = "Zendaya", profileImageRes = null)

    fun getRecommendedDestinations(): List<WishlistItem> = destinationsById(14, 11, 1)

    fun getUpcomingTrip(): UpcomingTrip? = UpcomingTrip(1, "Anuradhapura", "26 Aug 2026", "6 Days", R.drawable.anuradhapura)

    fun getWeather(): WeatherInfo? = WeatherInfo("Colombo", 29, "Sunny", 60, WeatherType.CLEAR)

    fun getPopularDestinations(): List<WishlistItem> = destinationsById(1, 11, 12, 14, 15)

    private fun destinationsById(vararg ids: Int): List<WishlistItem> {
        val destinations = WishlistDestinationCatalog.destinations.associateBy(WishlistItem::id)
        return ids.asList().mapNotNull(destinations::get)
    }
}
