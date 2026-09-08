package com.ekatayan.app.ui.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SETTINGS_ROUTE = "settings"

fun NavGraphBuilder.settingsScreen(
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    composable(SETTINGS_ROUTE) {
        SettingsRoute(
            onLogoutClick = onLogoutClick,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
        )
    }
}
