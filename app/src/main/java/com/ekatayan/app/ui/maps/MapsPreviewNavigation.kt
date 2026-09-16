package com.ekatayan.app.ui.maps

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val MAPS_PREVIEW_ROUTE = "maps/preview"

fun NavGraphBuilder.mapsPreviewScreen(onBackClick: () -> Unit) {
    composable(MAPS_PREVIEW_ROUTE) {
        MapsPreviewScreen(onBackClick = onBackClick)
    }
}
