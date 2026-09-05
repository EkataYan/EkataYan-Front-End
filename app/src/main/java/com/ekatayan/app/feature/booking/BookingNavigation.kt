package com.ekatayan.app.feature.booking

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val BOOKING_ROUTE = "booking"

fun NavGraphBuilder.bookingScreen(
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    composable(BOOKING_ROUTE) {
        BookingRoute(
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
        )
    }
}
