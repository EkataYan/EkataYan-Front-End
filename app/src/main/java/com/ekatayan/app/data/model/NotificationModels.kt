package com.ekatayan.app.data.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class NotificationCategory { TRIPS, EXPENSES, UPDATES }

enum class NotificationFilter { ALL, TRIPS, EXPENSES, UPDATES }

@Immutable
data class NotificationItem(
    val id: Int,
    @param:StringRes val titleRes: Int,
    @param:StringRes val messageRes: Int,
    @param:StringRes val timeLabelRes: Int,
    val category: NotificationCategory,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBackground: Color,
    val isUnread: Boolean,
)

@Immutable data class TripInvitation(
    val id:String,val tripId:String,val tripName:String,val startDate:String,val endDate:String,
    val inviterName:String,val inviterUsername:String,val inviterAvatarUrl:String?,val createdAt:String,
)
