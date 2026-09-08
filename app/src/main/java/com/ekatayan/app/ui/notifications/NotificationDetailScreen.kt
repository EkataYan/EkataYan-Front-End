package com.ekatayan.app.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.data.model.NotificationCategory
import com.ekatayan.app.data.model.NotificationItem

@Composable
fun NotificationDetailScreen(
    notification: NotificationItem?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize(), containerColor = Color(0xFFFCFCFC)) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.notification_detail_back))
                }
                Text(
                    text = stringResource(R.string.notification_detail_title),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            if (notification != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Box(
                            modifier = Modifier.size(54.dp).background(notification.iconBackground, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(notification.icon, contentDescription = null, tint = notification.iconTint, modifier = Modifier.size(27.dp))
                        }
                        Text(
                            text = stringResource(notification.titleRes),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF141212),
                            modifier = Modifier.padding(top = 18.dp),
                        )
                        Text(
                            text = stringResource(notification.messageRes),
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = Color(0xFF625B5B),
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        Row(
                            modifier = Modifier.padding(top = 22.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Text(stringResource(notification.timeLabelRes), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 6.dp))
                            Spacer(Modifier.weight(1f))
                            Text(stringResource(notification.category.labelRes), fontSize = 12.sp, color = notification.iconTint, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                Text(stringResource(R.string.notification_detail_not_found), color = Color.Gray)
            }
        }
    }
}

private val NotificationCategory.labelRes: Int
    get() = when (this) {
        NotificationCategory.TRIPS -> R.string.notifications_filter_trips
        NotificationCategory.EXPENSES -> R.string.notifications_filter_expenses
        NotificationCategory.UPDATES -> R.string.notifications_filter_updates
    }
