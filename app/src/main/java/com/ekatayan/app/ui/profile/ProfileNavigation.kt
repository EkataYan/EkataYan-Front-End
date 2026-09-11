package com.ekatayan.app.ui.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val PROFILE_ROUTE = "profile"
const val EDIT_PROFILE_ROUTE = "profile/edit"

fun NavGraphBuilder.profileScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
) {
    composable(PROFILE_ROUTE) {
        ProfileRoute(
            onBackClick = onBackClick,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onWishlistClick = onWishlistClick,
            onGroupsClick = onGroupsClick,
            onSettingsClick = onSettingsClick,
            onEditProfileClick = onEditProfileClick,
        )
    }
}

fun NavGraphBuilder.editProfileScreen(onBackClick: () -> Unit, onSaved: () -> Unit) {
    composable(EDIT_PROFILE_ROUTE) { EditProfileRoute(onBackClick = onBackClick, onSaved = onSaved) }
}
