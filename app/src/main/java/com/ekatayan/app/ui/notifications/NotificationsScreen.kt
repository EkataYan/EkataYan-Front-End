package com.ekatayan.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.data.model.NotificationCategory
import com.ekatayan.app.data.model.NotificationFilter
import com.ekatayan.app.data.model.NotificationItem
import com.ekatayan.app.viewmodel.NotificationsUiState

private val SelectedFilterBackground = Color(0xFFC8E9EF)
private val PrimaryText = Color(0xFF141212)
private val SecondaryText = Color(0xFF625B5B)
private val UnreadDot = Color(0xFF2DBE72)

@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    selectedBottomNavItem: AppBottomNavItem,
    onFilterSelected: (NotificationFilter) -> Unit,
    onNotificationClick: (Int) -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFFCFCFC),
        bottomBar = {
            AppBottomNavigation(
                selectedItem = selectedBottomNavItem,
                onHomeClick = onHomeClick,
                onTripsClick = onTripsClick,
                onPlannerClick = onPlannerClick,
                onExpensesClick = onExpensesClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 22.dp,
                top = innerPadding.calculateTopPadding() + 28.dp,
                end = 22.dp,
                bottom = innerPadding.calculateBottomPadding() + 18.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.notifications_title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                )
            }
            item { NotificationFilters(uiState.selectedFilter, onFilterSelected) }
            items(uiState.filteredNotifications, key = NotificationItem::id) { notification ->
                NotificationCard(notification = notification, onClick = { onNotificationClick(notification.id) })
            }
        }
    }
}

@Composable
private fun NotificationFilters(
    selectedFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        NotificationFilter.entries.forEach { filter ->
            val isSelected = filter == selectedFilter
            Surface(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(999.dp),
                color = if (isSelected) SelectedFilterBackground else Color.White,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E4E8)),
            ) {
                Text(
                    text = stringResource(filter.labelRes),
                    color = if (isSelected) PrimaryText else SecondaryText,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                )
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEFF2F6)),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(notification.iconBackground, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = notification.icon,
                    contentDescription = null,
                    tint = notification.iconTint,
                    modifier = Modifier.size(23.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = stringResource(notification.titleRes),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PrimaryText,
                )
                Text(
                    text = stringResource(notification.messageRes),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = SecondaryText,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp),
                        tint = SecondaryText,
                    )
                    Text(
                        text = stringResource(notification.timeLabelRes),
                        fontSize = 11.sp,
                        color = SecondaryText,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
            if (notification.isUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(UnreadDot, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                )
            } else {
                Spacer(Modifier.size(10.dp))
            }
        }
    }
}

private val NotificationFilter.labelRes: Int
    get() = when (this) {
        NotificationFilter.ALL -> R.string.notifications_filter_all
        NotificationFilter.TRIPS -> R.string.notifications_filter_trips
        NotificationFilter.EXPENSES -> R.string.notifications_filter_expenses
        NotificationFilter.UPDATES -> R.string.notifications_filter_updates
    }
