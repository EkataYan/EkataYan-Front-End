package com.ekatayan.app.feature.trips

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val TRIPS_ROUTE = "trips"
const val CREATE_TRIP_ROUTE = "trips/create"
const val TRIP_DETAILS_ROUTE = "trips/details/{tripId}"

fun NavGraphBuilder.tripsScreen(
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAddTripClick: () -> Unit,
    onTripClick: (Trip) -> Unit,
) {
    composable(TRIPS_ROUTE) {
        TripsRoute(
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            onAddTripClick = onAddTripClick,
            onTripClick = onTripClick,
        )
    }
}

fun NavGraphBuilder.createTripScreen(onBackClick: () -> Unit) {
    composable(CREATE_TRIP_ROUTE) { CreateTripRoute(onBackClick) }
}

fun NavGraphBuilder.tripDetailsScreen(onBackClick: () -> Unit) {
    composable(TRIP_DETAILS_ROUTE) { entry ->
        val tripId = entry.arguments?.getString("tripId")?.toIntOrNull()
        TripDetailsRoute(tripId = tripId, onBackClick = onBackClick)
    }
}
