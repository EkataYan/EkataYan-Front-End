package com.ekatayan.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.EkataErrorState
import com.ekatayan.app.core.designsystem.component.EkataLoadingState
import com.ekatayan.app.core.designsystem.component.EkataSectionHeading
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.core.designsystem.theme.EkataYanTheme
import com.ekatayan.app.data.local.HomeLocalDataSource
import com.ekatayan.app.viewmodel.HomeUiState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onNotificationClick: () -> Unit = {},
    hasUnreadNotifications: Boolean = false,
    onSettingsClick: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchSubmit: () -> Unit = {},
    onMapsClick: () -> Unit = {},
    onWishlistClick: () -> Unit = {},
    onBookingClick: () -> Unit = {},
    onGroupHubClick: () -> Unit = {},
    onPartnershipClick: () -> Unit = {},
    onRecommendedDestinationClick: (Int) -> Unit = {},
    onUpcomingTripClick: () -> Unit = {},
    onPopularDestinationClick: (Int) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (uiState.isLoading) {
            EkataLoadingState(
                message = stringResource(R.string.home_loading),
                modifier = Modifier.align(Alignment.Center).padding(EkataSpacing.lg),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = EkataSpacing.xxl * 2 + EkataSpacing.xl),
            ) {
                item {
                    HeroSection(
                        user = uiState.user,
                        searchQuery = uiState.searchQuery,
                        onNotificationClick = onNotificationClick,
                        hasUnreadNotifications = hasUnreadNotifications,
                        onSettingsClick = onSettingsClick,
                        onSearchQueryChange = onSearchQueryChange,
                        onSearchSubmit = onSearchSubmit,
                    )
                }
                item {
                    QuickActions(onMapsClick, onWishlistClick, onBookingClick, onGroupHubClick, onPartnershipClick)
                }
                uiState.errorMessage?.let { message ->
                    item {
                        EkataErrorState(
                            title = stringResource(R.string.home_error_title),
                            message = message,
                            modifier = Modifier.padding(horizontal = EkataSpacing.md),
                        )
                    }
                }
                item {
                    HomeSectionHeading(stringResource(R.string.home_recommended_title))
                }
                item { RecommendedSection(uiState.recommendedDestinations, onRecommendedDestinationClick) }
                item { HomeInfoCards(uiState.upcomingTrip, uiState.weather, onUpcomingTripClick, uiState.isWeatherLoading, uiState.weatherError) }
                item {
                    HomeSectionHeading(stringResource(R.string.home_popular_title))
                }
                item { PopularDestinationsSection(uiState.popularDestinations, onPopularDestinationClick) }
            }
        }
        AppBottomNavigation(
            selectedItem = AppBottomNavItem.HOME,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter),
            compact = true,
        )
    }
}

@Composable
private fun HomeSectionHeading(title: String) {
    EkataSectionHeading(
        title = title,
        modifier = Modifier.padding(
            start = EkataSpacing.md,
            end = EkataSpacing.md,
            top = EkataSpacing.sm,
            bottom = EkataSpacing.sm,
        ),
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    val localDataSource = HomeLocalDataSource()
    val previewState = HomeUiState(
        user = localDataSource.getUser(),
        recommendedDestinations = localDataSource.getRecommendedDestinations(),
        upcomingTrip = localDataSource.getUpcomingTrip(),
        weather = null,
        popularDestinations = localDataSource.getPopularDestinations(),
    )
    EkataYanTheme(darkTheme = false) {
        HomeScreen(
            uiState = previewState,
            onTripsClick = {},
            onPlannerClick = {},
            onExpensesClick = {},
            onProfileClick = {},
        )
    }
}
