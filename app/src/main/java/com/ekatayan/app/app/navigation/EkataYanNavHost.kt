package com.ekatayan.app.app.navigation

import com.ekatayan.app.feature.businesspartner.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ekatayan.app.feature.expenses.EXPENSES_ROUTE
import com.ekatayan.app.feature.expenses.expensesScreen
import com.ekatayan.app.feature.booking.BOOKING_ROUTE
import com.ekatayan.app.feature.booking.bookingScreen
import com.ekatayan.app.feature.home.HOME_ROUTE
import com.ekatayan.app.feature.home.homeScreen
import com.ekatayan.app.feature.grouphub.GROUP_HUB_ROUTE
import com.ekatayan.app.feature.grouphub.GroupHubViewModel
import com.ekatayan.app.feature.grouphub.groupChatRoute
import com.ekatayan.app.feature.grouphub.groupHubScreens
import com.ekatayan.app.feature.grouphub.groupInfoRoute
import com.ekatayan.app.feature.login.LOGIN_ROUTE
import com.ekatayan.app.feature.login.loginScreen
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.feature.notifications.NOTIFICATIONS_ROUTE
import com.ekatayan.app.feature.notifications.NotificationsViewModel
import com.ekatayan.app.feature.notifications.notificationDetailRoute
import com.ekatayan.app.feature.notifications.notificationDetailScreen
import com.ekatayan.app.feature.notifications.notificationsScreen
import com.ekatayan.app.feature.planner.PLANNER_ROUTE
import com.ekatayan.app.feature.planner.plannerScreen
import com.ekatayan.app.feature.profile.PROFILE_ROUTE
import com.ekatayan.app.feature.profile.profileScreen
import com.ekatayan.app.feature.signup.SIGN_UP_ROUTE
import com.ekatayan.app.feature.signup.signUpScreen
import com.ekatayan.app.feature.settings.SETTINGS_ROUTE
import com.ekatayan.app.feature.settings.settingsScreen
import com.ekatayan.app.feature.splash.SPLASH_ROUTE
import com.ekatayan.app.feature.splash.splashScreen
import com.ekatayan.app.feature.trips.CREATE_TRIP_ROUTE
import com.ekatayan.app.feature.trips.TRIPS_ROUTE
import com.ekatayan.app.feature.trips.createTripScreen
import com.ekatayan.app.feature.trips.tripDetailsScreen
import com.ekatayan.app.feature.trips.tripsScreen
import com.ekatayan.app.feature.wishlist.WISHLIST_ROUTE
import com.ekatayan.app.feature.wishlist.WishlistViewModel
import com.ekatayan.app.feature.wishlist.wishlistGroupRoute
import com.ekatayan.app.feature.wishlist.wishlistScreens

@Composable
fun EkataYanNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val businessPartnerViewModel: BusinessPartnerViewModel = hiltViewModel()
    val wishlistViewModel: WishlistViewModel = hiltViewModel()
    val groupHubViewModel: GroupHubViewModel = hiltViewModel()
    val notificationsViewModel: NotificationsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = SPLASH_ROUTE,
        modifier = modifier,
    ) {
        splashScreen(onSplashFinished = navController::navigateToLoginFromSplash)
        loginScreen(
            onLogInClick = navController::navigateHomeFromAuth,
            onSignUpClick = navController::navigateToSignUp,
        )
        signUpScreen(
            onSignUpClick = navController::navigateHomeFromAuth,
            onLoginClick = navController::navigateToLogin,
        )
        businessPartnerScreens(
            viewModel = businessPartnerViewModel,
            onNavigate = { navController.navigate(it) { launchSingleTop = true } },
            onBack = { navController.navigateUp() },
            onHome = { navController.navigate(HOME_ROUTE) { popUpTo(HOME_ROUTE); launchSingleTop = true } },
            onTab = { target -> navController.navigate(target) { popUpTo(PARTNER_HOME_ROUTE); launchSingleTop = true } },
            onLoggedIn = { navController.navigate(PARTNER_HOME_ROUTE) { popUpTo(PARTNER_ENTRY_ROUTE) { inclusive = true }; launchSingleTop = true } },
            onRestartFlow = { navController.navigate(PARTNER_ENTRY_ROUTE) { popUpTo(HOME_ROUTE); launchSingleTop = true } },
        )
        homeScreen(
            onPartnershipClick = { navController.navigate(PARTNER_ENTRY_ROUTE) { launchSingleTop = true } },
            onGroupHubClick = { navController.navigate(GROUP_HUB_ROUTE) },
            onWishlistClick = { navController.navigate(WISHLIST_ROUTE) },
            onBookingClick = { navController.navigate(BOOKING_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) },
            onSettingsClick = navController::navigateToSettings,
            onNotificationClick = navController::navigateToNotifications,
            notificationsUiState = notificationsViewModel.uiState,
        )
        bookingScreen(
            onHomeClick = { navController.navigate(HOME_ROUTE) },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) },
        )
        plannerScreen(onBackClick = navController::navigateUp)
        tripsScreen(
            onHomeClick = { navController.navigate(HOME_ROUTE) },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) },
            onAddTripClick = {
                navController.navigate(CREATE_TRIP_ROUTE)
            },
            onTripClick = { trip ->
                navController.navigate("trips/details/${trip.id}")
            },
        )
        createTripScreen(
            onBackClick = navController::navigateUp
        )

        tripDetailsScreen(
            onBackClick = navController::navigateUp
        )
        expensesScreen(
            onHomeClick = { navController.navigate(HOME_ROUTE) },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) },
            onSettingsClick = navController::navigateToSettings,
            onNotificationClick = navController::navigateToNotifications,
            notificationsUiState = notificationsViewModel.uiState,
        )
        profileScreen(
            onBackClick = navController::navigateUp,
            onHomeClick = { navController.navigate(HOME_ROUTE) },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onSettingsClick = navController::navigateToSettings,
            onNotificationClick = navController::navigateToNotifications,
            notificationsUiState = notificationsViewModel.uiState,
        )
        settingsScreen(
            onLogoutClick = {},
            onHomeClick = { navController.navigate(HOME_ROUTE) },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) { launchSingleTop = true } },
        )
        notificationsScreen(
            viewModel = notificationsViewModel,
            selectedBottomNavItem = { navController.previousTopLevelItem() },
            onHomeClick = { navController.navigate(HOME_ROUTE) },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) },
            onNotificationClick = { navController.navigate(notificationDetailRoute(it)) },
        )
        notificationDetailScreen(
            viewModel = notificationsViewModel,
            onBackClick = navController::navigateUp,
        )
        wishlistScreens(
            viewModel = wishlistViewModel,
            onGroupClick = { navController.navigate(wishlistGroupRoute(it)) },
            onBackClick = navController::navigateUp,
            onHomeClick = { navController.navigate(HOME_ROUTE) { launchSingleTop = true } },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) },
            onSettingsClick = navController::navigateToSettings,
            onNotificationClick = navController::navigateToNotifications,
            notificationsUiState = notificationsViewModel.uiState,
        )
        groupHubScreens(
            viewModel = groupHubViewModel,
            onGroupClick = { navController.navigate(groupChatRoute(it)) },
            onInfoClick = { navController.navigate(groupInfoRoute(it)) },
            onBackClick = navController::navigateUp,
            onRemoved = { navController.popBackStack(GROUP_HUB_ROUTE, inclusive = false) },
            onHomeClick = { navController.navigate(HOME_ROUTE) { launchSingleTop = true } },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onPlannerClick = { navController.navigate(PLANNER_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) },
            onSettingsClick = navController::navigateToSettings,
            onNotificationClick = navController::navigateToNotifications,
            notificationsUiState = notificationsViewModel.uiState,
        )
    }
}

private fun NavHostController.navigateToSignUp() {
    navigate(SIGN_UP_ROUTE) { launchSingleTop = true }
}

private fun NavHostController.navigateToLoginFromSplash() {
    navigate(LOGIN_ROUTE) {
        popUpTo(SPLASH_ROUTE) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToLogin() {
    navigate(LOGIN_ROUTE) {
        popUpTo(LOGIN_ROUTE)
        launchSingleTop = true
    }
}

private fun NavHostController.navigateHomeFromAuth() {
    navigate(HOME_ROUTE) {
        popUpTo(LOGIN_ROUTE) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.navigateToSettings() {
    navigate(SETTINGS_ROUTE) { launchSingleTop = true }
}

private fun NavHostController.navigateToNotifications() {
    navigate(NOTIFICATIONS_ROUTE) { launchSingleTop = true }
}

private fun NavHostController.previousTopLevelItem(): AppBottomNavItem =
    when (previousBackStackEntry?.destination?.route) {
        TRIPS_ROUTE -> AppBottomNavItem.TRIPS
        PLANNER_ROUTE -> AppBottomNavItem.PLANNER
        EXPENSES_ROUTE -> AppBottomNavItem.EXPENSES
        PROFILE_ROUTE, SETTINGS_ROUTE -> AppBottomNavItem.PROFILE
        else -> AppBottomNavItem.HOME
    }