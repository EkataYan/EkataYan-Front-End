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
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
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
            onNotificationClick = onNotificationClick,
            onSettingsClick = onSettingsClick,
        )
    }
}

fun NavGraphBuilder.createTripScreen(onBackClick: () -> Unit) {
    composable("$CREATE_TRIP_ROUTE?destination={destination}&start={start}&end={end}&budget={budget}&preferences={preferences}") { entry ->
        CreateTripRoute(onBackClick, entry.arguments?.getString("destination"), entry.arguments?.getString("start"), entry.arguments?.getString("end"), entry.arguments?.getString("budget"), entry.arguments?.getString("preferences"))
    }
}

fun NavGraphBuilder.tripDetailsScreen(onBackClick: () -> Unit) {
    composable(TRIP_DETAILS_ROUTE) { entry ->
        val tripId = entry.arguments?.getString("tripId")?.toIntOrNull()
        TripDetailsRoute(tripId = tripId, onBackClick = onBackClick)
    }
}
