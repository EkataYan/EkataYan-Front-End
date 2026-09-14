package com.ekatayan.app.ui.trips

import android.net.Uri
import com.ekatayan.app.data.model.Trip

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ekatayan.app.viewmodel.NotificationsUiState
import kotlinx.coroutines.flow.StateFlow

const val TRIPS_ROUTE = "trips"
const val CREATE_TRIP_ROUTE = "trips/create"
const val TRIP_DETAILS_ROUTE = "trips/details/{tripKey}"
const val TRIP_MEMBERS_ROUTE = "trips/{tripId}/members"
const val ADD_TRIP_MEMBER_ROUTE = "trips/{tripId}/members/add"

fun tripDetailsRoute(tripKey: String): String = "trips/details/${Uri.encode(tripKey)}"

fun tripDetailsRoute(trip: Trip): String = tripDetailsRoute(trip.remoteId ?: trip.id.toString())

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
    notificationsUiState: StateFlow<NotificationsUiState>,
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
            notificationsUiState = notificationsUiState,
        )
    }
}

fun NavGraphBuilder.createTripScreen(onBackClick: () -> Unit) {
    composable("$CREATE_TRIP_ROUTE?destination={destination}&start={start}&end={end}&budget={budget}&preferences={preferences}") { entry ->
        CreateTripRoute(onBackClick)
    }
}

fun NavGraphBuilder.tripDetailsScreen(onBackClick: () -> Unit, onMembersClick: (String) -> Unit, onExpensesClick: (String) -> Unit) {
    composable(TRIP_DETAILS_ROUTE) { entry ->
        val tripKey = entry.arguments?.getString("tripKey")
        TripDetailsRoute(tripKey = tripKey, onBackClick = onBackClick, onMembersClick = onMembersClick, onExpensesClick = onExpensesClick)
    }
}

fun NavGraphBuilder.tripMemberScreens(onBackClick:()->Unit,onAdd:(String)->Unit) {
    composable(TRIP_MEMBERS_ROUTE) { entry -> TripMembersRoute(false,onBackClick,{ entry.arguments?.getString("tripId")?.let(onAdd) }) }
    composable(ADD_TRIP_MEMBER_ROUTE) { TripMembersRoute(true,onBackClick,{}) }
}
