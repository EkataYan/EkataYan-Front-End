package com.ekatayan.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import com.ekatayan.app.core.designsystem.theme.EkataComponentSize
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary

val HeaderActionsTopPadding = EkataComponentSize.pageHeaderTop

@Composable
fun HeaderActions(
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasUnreadNotifications: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        IconButton(onClick = onNotificationClick, modifier = Modifier.size(EkataComponentSize.pageHeaderAction)) {
            Box(modifier = Modifier.size(26.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notifications",
                tint = EkataTextPrimary,
                modifier = Modifier.size(21.dp),
            )
                if (hasUnreadNotifications) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(8.dp)
                            .background(Color(0xFFFF8A8A), CircleShape),
                    )
                }
            }
        }
        IconButton(onClick = onSettingsClick, modifier = Modifier.size(EkataComponentSize.pageHeaderAction)) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = EkataTextPrimary,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}
