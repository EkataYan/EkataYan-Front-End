package com.ekatayan.app.ui.destinationdetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.data.local.DestinationDetailsCatalog
import com.ekatayan.app.viewmodel.GroupHubViewModel
import com.ekatayan.app.viewmodel.WishlistViewModel

@Composable
fun DestinationDetailsRoute(
    destinationId: String,
    wishlistViewModel: WishlistViewModel,
    groupHubViewModel: GroupHubViewModel,
    onBackClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
) {
    val wishlistState by wishlistViewModel.uiState.collectAsStateWithLifecycle()
    val groupHubState by groupHubViewModel.uiState.collectAsStateWithLifecycle()
    val destination = DestinationDetailsCatalog.destination(destinationId)
    val wishlistItem = destination?.let { details ->
        wishlistState.availableDestinations.find { it.id == details.wishlistItemId }
    }
    DestinationDetailsScreen(
        destination = destination,
        popularPlaces = destination?.let(DestinationDetailsCatalog::popularPlaces).orEmpty(),
        wishlistItem = wishlistItem,
        wishlistGroups = wishlistState.groups,
        groupHubState = groupHubState,
        onBackClick = onBackClick,
        onSharePlace = groupHubViewModel::sharePlace,
        onPlaceClick = onPlaceClick,
        onGroupSelectionChange = { groupId, item, selected ->
            if (selected) wishlistViewModel.addPlaceToGroup(groupId, item)
            else wishlistViewModel.removePlaceFromGroup(groupId, item.id)
        },
        onCreateGroupWithPlace = wishlistViewModel::createGroupWithPlace,
    )
}

@Composable
fun PlaceDetailsRoute(
    placeId: String,
    wishlistViewModel: WishlistViewModel,
    groupHubViewModel: GroupHubViewModel,
    onBackClick: () -> Unit,
) {
    val wishlistState by wishlistViewModel.uiState.collectAsStateWithLifecycle()
    val groupHubState by groupHubViewModel.uiState.collectAsStateWithLifecycle()
    val attraction = DestinationDetailsCatalog.attraction(placeId)
    val wishlistItem = attraction?.let { place ->
        wishlistState.availableDestinations.find { it.id == place.wishlistItemId }
    }
    PlaceDetailsScreen(
        attraction = attraction,
        wishlistItem = wishlistItem,
        wishlistGroups = wishlistState.groups,
        groupHubState = groupHubState,
        onBackClick = onBackClick,
        onSharePlace = groupHubViewModel::sharePlace,
        onGroupSelectionChange = { groupId, item, selected ->
            if (selected) wishlistViewModel.addPlaceToGroup(groupId, item)
            else wishlistViewModel.removePlaceFromGroup(groupId, item.id)
        },
        onCreateGroupWithPlace = wishlistViewModel::createGroupWithPlace,
    )
}
