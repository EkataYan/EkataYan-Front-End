package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.HomeLocalDataSource
import com.ekatayan.app.data.model.PopularDestination
import com.ekatayan.app.data.model.RecommendedDestination
import com.ekatayan.app.data.model.UpcomingTrip
import com.ekatayan.app.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

interface HomeRepository {
    fun getUser(): User
    fun getRecommendedDestinations(): List<RecommendedDestination>
    fun getUpcomingTrip(): UpcomingTrip?
    fun getPopularDestinations(): List<PopularDestination>
}

@Singleton
class DefaultHomeRepository @Inject constructor(
    private val localDataSource: HomeLocalDataSource,
) : HomeRepository {

    override fun getUser(): User = localDataSource.getUser()

    override fun getRecommendedDestinations(): List<RecommendedDestination> =
        localDataSource.getRecommendedDestinations()

    override fun getUpcomingTrip(): UpcomingTrip? = localDataSource.getUpcomingTrip()

    override fun getPopularDestinations(): List<PopularDestination> =
        localDataSource.getPopularDestinations()
}
