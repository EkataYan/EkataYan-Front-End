package com.ekatayan.app.data.model

import java.time.LocalDate

data class Trip(
    val id: Int,
    val nameRes: Int,
    val locationRes: Int,
    val statusRes: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val imageRes: Int,
    val customName: String? = null,
    val customLocation: String? = null,
    val budget: String? = null,
    val notes: String? = null,
    val imageUri: String? = null,
    val remoteId: String? = null,
    val source: String = "manual",
    val summary: String? = null,
    val route: List<String> = emptyList(),
    val travellerType: String? = null,
    val travellerCount: Int? = null,
    val travelStyle: String? = null,
    val travelPace: String? = null,
    val canDelete: Boolean = false,
)

