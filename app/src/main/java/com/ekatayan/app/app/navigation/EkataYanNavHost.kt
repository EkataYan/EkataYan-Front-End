package com.ekatayan.app.app.navigation

import com.ekatayan.app.ui.businesspartner.*
import com.ekatayan.app.viewmodel.BusinessPartnerViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ekatayan.app.ui.expenses.EXPENSES_ROUTE
import com.ekatayan.app.ui.expenses.expensesScreen
import com.ekatayan.app.ui.booking.BOOKING_ROUTE
import com.ekatayan.app.ui.booking.bookingScreen
import com.ekatayan.app.ui.home.HOME_ROUTE
import com.ekatayan.app.ui.home.homeScreen
import com.ekatayan.app.ui.grouphub.GROUP_HUB_ROUTE
import com.ekatayan.app.viewmodel.GroupHubViewModel
import com.ekatayan.app.ui.grouphub.groupChatRoute
import com.ekatayan.app.ui.grouphub.groupHubScreens
import com.ekatayan.app.ui.grouphub.groupInfoRoute
import com.ekatayan.app.ui.login.LOGIN_ROUTE
import com.ekatayan.app.ui.login.loginScreen
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.viewmodel.NotificationsViewModel
import com.ekatayan.app.ui.notifications.NOTIFICATIONS_ROUTE
import com.ekatayan.app.ui.notifications.notificationDetailRoute
import com.ekatayan.app.ui.notifications.notificationDetailScreen
import com.ekatayan.app.ui.notifications.notificationsScreen
import com.ekatayan.app.ui.planner.PLANNER_ROUTE
import com.ekatayan.app.ui.planner.plannerScreen
import com.ekatayan.app.ui.profile.PROFILE_ROUTE
import com.ekatayan.app.ui.profile.EDIT_PROFILE_ROUTE
import com.ekatayan.app.ui.profile.editProfileScreen
import com.ekatayan.app.ui.profile.profileScreen
import com.ekatayan.app.ui.signup.SIGN_UP_ROUTE
import com.ekatayan.app.ui.signup.signUpScreen
import com.ekatayan.app.ui.settings.SETTINGS_ROUTE
import com.ekatayan.app.ui.settings.settingsScreen
import com.ekatayan.app.ui.splash.SPLASH_ROUTE
import com.ekatayan.app.ui.splash.splashScreen
import com.ekatayan.app.ui.welcome.WELCOME_ROUTE
import com.ekatayan.app.ui.welcome.WelcomePreferences
import com.ekatayan.app.ui.welcome.welcomeScreen
import com.ekatayan.app.ui.trips.CREATE_TRIP_ROUTE
import com.ekatayan.app.ui.trips.TRIPS_ROUTE
import com.ekatayan.app.ui.trips.createTripScreen
import com.ekatayan.app.ui.trips.tripDetailsScreen
import com.ekatayan.app.ui.trips.tripMemberScreens
import com.ekatayan.app.ui.trips.tripsScreen
import com.ekatayan.app.ui.wishlist.WISHLIST_ROUTE
import com.ekatayan.app.viewmodel.WishlistViewModel
import com.ekatayan.app.ui.wishlist.wishlistGroupRoute
import com.ekatayan.app.ui.wishlist.wishlistScreens
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun EkataYanNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val welcomePreferences = remember(context) { WelcomePreferences(context) }
    val preferenceScope = androidx.compose.runtime.rememberCoroutineScope()
    val businessPartnerViewModel: BusinessPartnerViewModel = hiltViewModel()
    val wishlistViewModel: WishlistViewModel = hiltViewModel()
    val groupHubViewModel: GroupHubViewModel = hiltViewModel()
    val notificationsViewModel: NotificationsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = SPLASH_ROUTE,
        modifier = modifier,
    ) {
        splashScreen(onSplashFinished = {
            if (welcomePreferences.hasCompletedWelcome()) {
                navController.navigateToLoginFromSplash()
            } else {
                navController.navigateToWelcomeFromSplash()
            }
        })
        welcomeScreen(onGetStarted = {
            preferenceScope.launch {
                welcomePreferences.markWelcomeCompleted()
                navController.navigate(LOGIN_ROUTE) {
                    popUpTo(WELCOME_ROUTE) { inclusive = true }
                    launchSingleTop = true
                }
            }
        })
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
            wishlistViewModel = wishlistViewModel,
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
            onNotificationClick = navController::navigateToNotifications,
            onSettingsClick = navController::navigateToSettings,
            notificationsUiState = notificationsViewModel.uiState,
        )
        plannerScreen(
            onHomeClick = { navController.navigate(HOME_ROUTE) },
            onTripsClick = { navController.navigate(TRIPS_ROUTE) },
            onExpensesClick = { navController.navigate(EXPENSES_ROUTE) },
            onProfileClick = { navController.navigate(PROFILE_ROUTE) },
            onNotificationClick = navController::navigateToNotifications,
            onSettingsClick = navController::navigateToSettings,
            notificationsUiState = notificationsViewModel.uiState,
        )
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
                navController.navigate("trips/details/${Uri.encode(trip.remoteId ?: trip.id.toString())}")
            },
            onNotificationClick = navController::navigateToNotifications,
            onSettingsClick = navController::navigateToSettings,
            notificationsUiState = notificationsViewModel.uiState,
        )
        createTripScreen(onBackClick = navController::navigateUp)

        tripDetailsScreen(
            onBackClick = navController::navigateUp,
            onMembersClick = { navController.navigate("trips/$it/members") },
        )
        tripMemberScreens(navController::navigateUp) { navController.navigate("trips/$it/members/add") }
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
            onWishlistClick = { navController.navigate(WISHLIST_ROUTE) },
            onGroupsClick = { navController.navigate(GROUP_HUB_ROUTE) },
            onSettingsClick = navController::navigateToSettings,
            onEditProfileClick = { navController.navigate(EDIT_PROFILE_ROUTE) },
        )
        editProfileScreen(
            onBackClick = navController::navigateUp,
            onSaved = navController::navigateUp,
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

private fun NavHostController.navigateToWelcomeFromSplash() {
    navigate(WELCOME_ROUTE) {
        popUpTo(SPLASH_ROUTE) { inclusive = true }
        launchSingleTop = true
    }
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
