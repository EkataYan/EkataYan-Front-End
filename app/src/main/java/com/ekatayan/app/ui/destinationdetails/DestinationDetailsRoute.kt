package com.ekatayan.app.ui.destinationdetails

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.R
import com.ekatayan.app.data.local.DestinationDetailsCatalog
import com.ekatayan.app.viewmodel.WishlistViewModel

@Composable
fun DestinationDetailsRoute(
    destinationId: String,
    wishlistViewModel: WishlistViewModel,
    onBackClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
) {
    val wishlistState by wishlistViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val destination = DestinationDetailsCatalog.destination(destinationId)
    val wishlistItem = destination?.let { details ->
        wishlistState.availableDestinations.find { it.id == details.wishlistItemId }
    }
    DestinationDetailsScreen(
        destination = destination,
        popularPlaces = destination?.let(DestinationDetailsCatalog::popularPlaces).orEmpty(),
        wishlistItem = wishlistItem,
        wishlistGroups = wishlistState.groups,
        onBackClick = onBackClick,
        onShareClick = destination?.let { details ->
            { sharePlace(context, details.name, details.description) }
        } ?: {},
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
    onBackClick: () -> Unit,
) {
    val wishlistState by wishlistViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val attraction = DestinationDetailsCatalog.attraction(placeId)
    val wishlistItem = attraction?.let { place ->
        wishlistState.availableDestinations.find { it.id == place.wishlistItemId }
    }
    PlaceDetailsScreen(
        attraction = attraction,
        wishlistItem = wishlistItem,
        wishlistGroups = wishlistState.groups,
        onBackClick = onBackClick,
        onShareClick = attraction?.let { place ->
            { sharePlace(context, place.name, place.shortDescription) }
        } ?: {},
        onGroupSelectionChange = { groupId, item, selected ->
            if (selected) wishlistViewModel.addPlaceToGroup(groupId, item)
            else wishlistViewModel.removePlaceFromGroup(groupId, item.id)
        },
        onCreateGroupWithPlace = wishlistViewModel::createGroupWithPlace,
    )
}

private fun sharePlace(context: Context, name: String, description: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, name)
        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.destination_details_share_text, name, description))
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.destination_details_share)))
}
