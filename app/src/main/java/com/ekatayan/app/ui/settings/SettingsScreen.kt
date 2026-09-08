package com.ekatayan.app.ui.settings

import com.ekatayan.app.data.model.SettingsPreferences

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation

private val RowBackground = Color(0xFFEBF6F8)
private val SectionBackground = Color(0xFFF5F5F5)
private val PrimaryText = Color(0xFF141212)
private val SecondaryText = Color(0xFF625B5B)
private val LogoutRed = Color(0xFFFF383C)

@Composable
fun SettingsScreen(
    uiState: SettingsPreferences,
    onPushNotificationsChanged: (Boolean) -> Unit,
    onDarkModeChanged: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            AppBottomNavigation(
                selectedItem = AppBottomNavItem.PROFILE,
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
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    color = Color.Black,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            item { UserProfileCard(uiState = uiState) }
            item {
                SettingsSection(title = stringResource(R.string.settings_account)) {
                    SettingsRow(Icons.Outlined.Person, stringResource(R.string.settings_profile_information), stringResource(R.string.settings_profile_information_subtitle))
                    SettingsRow(Icons.Outlined.Lock, stringResource(R.string.settings_change_password), stringResource(R.string.settings_change_password_subtitle))
                    SettingsRow(Icons.Outlined.Email, stringResource(R.string.settings_email_preferences), stringResource(R.string.settings_email_preferences_subtitle))
                    SettingsRow(Icons.Outlined.Security, stringResource(R.string.settings_privacy_security), stringResource(R.string.settings_privacy_security_subtitle))
                }
            }
            item {
                SettingsSection(title = stringResource(R.string.settings_preferences)) {
                    SettingsRow(Icons.Outlined.Language, stringResource(R.string.settings_languages), uiState.selectedLanguage)
                    SettingsRow(Icons.Outlined.AttachMoney, stringResource(R.string.settings_currency), uiState.selectedCurrency)
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(R.string.settings_push_notifications),
                        subtitle = stringResource(R.string.settings_push_notifications_subtitle),
                        checked = uiState.pushNotificationsEnabled,
                        onCheckedChange = onPushNotificationsChanged,
                    )
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.settings_dark_mode),
                        subtitle = stringResource(R.string.settings_dark_mode_subtitle),
                        checked = uiState.darkModeEnabled,
                        onCheckedChange = onDarkModeChanged,
                    )
                }
            }
            item {
                SettingsSection(title = stringResource(R.string.settings_support)) {
                    SettingsRow(Icons.AutoMirrored.Outlined.HelpOutline, stringResource(R.string.settings_help_center), stringResource(R.string.settings_help_center_subtitle))
                    SettingsRow(Icons.Outlined.Phone, stringResource(R.string.settings_contact_us), stringResource(R.string.settings_contact_us_subtitle))
                    SettingsRow(Icons.Outlined.Description, stringResource(R.string.settings_terms), stringResource(R.string.settings_terms_subtitle))
                    SettingsRow(Icons.Outlined.Info, stringResource(R.string.settings_about), uiState.appVersion)
                }
            }
            item { LogoutButton(onClick = onLogoutClick) }
        }
    }
}

@Composable
private fun UserProfileCard(uiState: SettingsPreferences, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = RowBackground,
        shadowElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Color(0xFFD6E4E7)) {
                if (uiState.profileImageResId != null) {
                    Image(
                        painter = painterResource(uiState.profileImageResId),
                        contentDescription = stringResource(R.string.settings_profile_image),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = stringResource(R.string.settings_profile_image),
                        tint = SecondaryText,
                        modifier = Modifier.padding(13.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
                Text(uiState.userName, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = PrimaryText)
                Spacer(Modifier.height(3.dp))
                Text(uiState.userEmail, fontSize = 11.sp, color = SecondaryText)
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(15.dp), tint = SecondaryText)
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(title, fontSize = 11.sp, color = PrimaryText, modifier = Modifier.padding(start = 2.dp, bottom = 8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            color = SectionBackground,
            shadowElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = content,
            )
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showChevron: Boolean = true,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(RowBackground)
            .clickable(enabled = checked == null, onClick = onClick)
            .padding(start = 16.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp), tint = PrimaryText)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 18.dp)) {
            Text(title, fontSize = 12.sp, color = PrimaryText, maxLines = 1)
            Text(subtitle, fontSize = 9.sp, color = SecondaryText, maxLines = 1)
        }
        if (checked != null && onCheckedChange != null) {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.size(width = 46.dp, height = 28.dp),
                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF4F9DA6)),
            )
        } else if (showChevron) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(13.dp), tint = SecondaryText)
        }
    }
}

@Composable
private fun LogoutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SectionBackground)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, modifier = Modifier.size(19.dp), tint = LogoutRed)
        Text(stringResource(R.string.settings_logout), color = LogoutRed, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
    }
}
