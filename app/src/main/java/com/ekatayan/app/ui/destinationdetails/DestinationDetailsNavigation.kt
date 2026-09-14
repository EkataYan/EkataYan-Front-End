package com.ekatayan.app.ui.destinationdetails

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ekatayan.app.viewmodel.WishlistViewModel
import com.ekatayan.app.viewmodel.GroupHubViewModel

const val DESTINATION_DETAILS_ROUTE = "destination/{destinationId}"
const val PLACE_DETAILS_ROUTE = "place/{placeId}"
private const val DESTINATION_ID_ARGUMENT = "destinationId"
private const val PLACE_ID_ARGUMENT = "placeId"

fun destinationDetailsRoute(destinationId: String) = "destination/$destinationId"
fun placeDetailsRoute(placeId: String) = "place/$placeId"

fun NavGraphBuilder.destinationDetailsScreens(
    wishlistViewModel: WishlistViewModel,
    groupHubViewModel: GroupHubViewModel,
    onBackClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
) {
    composable(
        route = DESTINATION_DETAILS_ROUTE,
        arguments = listOf(navArgument(DESTINATION_ID_ARGUMENT) { type = NavType.StringType }),
    ) { entry ->
        DestinationDetailsRoute(
            destinationId = requireNotNull(entry.arguments?.getString(DESTINATION_ID_ARGUMENT)),
            wishlistViewModel = wishlistViewModel,
            groupHubViewModel = groupHubViewModel,
            onBackClick = onBackClick,
            onPlaceClick = onPlaceClick,
        )
    }
    composable(
        route = PLACE_DETAILS_ROUTE,
        arguments = listOf(navArgument(PLACE_ID_ARGUMENT) { type = NavType.StringType }),
    ) { entry ->
        PlaceDetailsRoute(
            placeId = requireNotNull(entry.arguments?.getString(PLACE_ID_ARGUMENT)),
            wishlistViewModel = wishlistViewModel,
            groupHubViewModel = groupHubViewModel,
            onBackClick = onBackClick,
        )
    }
}
