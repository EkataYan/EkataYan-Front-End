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
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.EkataEmptyState
import com.ekatayan.app.core.designsystem.theme.*
import com.ekatayan.app.data.model.NotificationCategory
import com.ekatayan.app.data.model.NotificationFilter
import com.ekatayan.app.data.model.NotificationItem
import com.ekatayan.app.data.model.TripInvitation
import com.ekatayan.app.viewmodel.NotificationsUiState

private val UnreadDot = Color(0xFF2DBE72)

@Composable
fun NotificationsScreen(
    uiState: NotificationsUiState,
    selectedBottomNavItem: AppBottomNavItem,
    onFilterSelected: (NotificationFilter) -> Unit,
    onNotificationClick: (String) -> Unit,
    onAcceptInvite:(String)->Unit,
    onDeclineInvite:(String)->Unit,
    onRetryInvites:()->Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
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
                start = EkataSpacing.pageHorizontal,
                top = innerPadding.calculateTopPadding() + EkataComponentSize.pageHeaderTop,
                end = EkataSpacing.pageHorizontal,
                bottom = innerPadding.calculateBottomPadding() + EkataSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.notifications_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            item { NotificationFilters(uiState.selectedFilter, onFilterSelected) }
            if(uiState.selectedFilter in setOf(NotificationFilter.ALL,NotificationFilter.TRIPS)) {
                if(uiState.loadingInvitations) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                if(uiState.invitationError!=null) item { TextButton(onClick=onRetryInvites){Text(stringResource(R.string.ui_couldn_t_load_invitations_retry))} }
                items(uiState.invitations,key={"invite-${it.id}"}) { invite ->
                    InvitationCard(invite, uiState.respondingInvites[invite.id], onAcceptInvite, onDeclineInvite)
                }
            }
            items(uiState.filteredNotifications, key = NotificationItem::id) { notification ->
                NotificationCard(notification = notification, onClick = { onNotificationClick(notification.id) })
            }
            if (!uiState.loadingInvitations && uiState.invitationError == null &&
                uiState.invitations.isEmpty() && uiState.filteredNotifications.isEmpty()
            ) {
                item {
                    EkataEmptyState(
                        title = stringResource(R.string.notifications_empty_title),
                        message = stringResource(R.string.notifications_empty_message),
                    )
                }
            }
        }
    }
}

@Composable
private fun InvitationCard(invite: TripInvitation, action: String?, accept: (String) -> Unit, decline: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(EkataStroke.thin, MaterialTheme.colorScheme.outline),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.ui_trip_invitation), style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.invitation_from_user,invite.inviterName), modifier = Modifier.padding(top = 8.dp))
            Text(invite.tripName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("${invite.startDate} - ${invite.endDate}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { decline(invite.id) }, modifier = Modifier.weight(1f), enabled = action == null) {
                    Text(stringResource(if (action == "declining") R.string.declining else R.string.decline))
                }
                Button(onClick = { accept(invite.id) }, modifier = Modifier.weight(1f), enabled = action == null) {
                    Text(stringResource(if (action == "joining") R.string.joining else R.string.join_trip))
                }
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
                modifier = Modifier.clip(RoundedCornerShape(EkataRadius.pill)).clickable { onFilterSelected(filter) },
                shape = RoundedCornerShape(EkataRadius.pill),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(EkataStroke.thin, MaterialTheme.colorScheme.outline),
            ) {
                Text(
                    text = stringResource(filter.labelRes),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium),
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
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(EkataStroke.thin, MaterialTheme.colorScheme.outline),
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
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = notification.timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
