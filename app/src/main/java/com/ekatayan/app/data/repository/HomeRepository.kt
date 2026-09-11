package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.HomeLocalDataSource
import com.ekatayan.app.data.model.UpcomingTrip
import com.ekatayan.app.data.model.User
import com.ekatayan.app.data.model.WeatherInfo
import com.ekatayan.app.data.model.WishlistItem
import javax.inject.Inject
import javax.inject.Singleton

interface HomeRepository {
    fun getUser(): User
    fun getRecommendedDestinations(): List<WishlistItem>
    fun getUpcomingTrip(): UpcomingTrip?
    fun getWeather(): WeatherInfo?
    fun getPopularDestinations(): List<WishlistItem>
}

@Singleton
class DefaultHomeRepository @Inject constructor(
    private val localDataSource: HomeLocalDataSource,
) : HomeRepository {

    override fun getUser(): User = localDataSource.getUser()

    override fun getRecommendedDestinations(): List<WishlistItem> =
        localDataSource.getRecommendedDestinations()

    override fun getUpcomingTrip(): UpcomingTrip? = localDataSource.getUpcomingTrip()

    override fun getWeather(): WeatherInfo? = localDataSource.getWeather()

    override fun getPopularDestinations(): List<WishlistItem> =
        localDataSource.getPopularDestinations()
}
