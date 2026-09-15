package com.ekatayan.app.data.model

import androidx.annotation.DrawableRes

data class DestinationDetails(
    val id: String,
    val wishlistItemId: Int,
    val name: String,
    val location: String,
    val description: String,
    @param:DrawableRes val imageRes: Int,
    val categories: List<String> = emptyList(),
    val popularPlaceIds: List<String> = emptyList(),
)

data class Attraction(
    val id: String,
    val wishlistItemId: Int,
    val parentDestinationId: String,
    val name: String,
    val location: String,
    val shortDescription: String,
    val description: String,
    @param:DrawableRes val imageRes: Int,
    val categories: List<String> = emptyList(),
    val bestTime: String? = null,
    val entryFee: String? = null,
    val idealFor: List<String> = emptyList(),
    val highlights: List<AttractionHighlight> = emptyList(),
    val tips: List<String> = emptyList(),
)

data class AttractionHighlight(
    val title: String,
    @param:DrawableRes val imageRes: Int,
)
