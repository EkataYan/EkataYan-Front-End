package com.ekatayan.app.ui.settings

import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.EkataSectionHeading
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding
import com.ekatayan.app.core.designsystem.theme.EkataElevation
import com.ekatayan.app.core.designsystem.theme.EkataIconSize
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.data.model.SettingsPreferences

private val SettingsRowHeight = 76.dp
private val SettingsIconContainerSize = 40.dp

@Composable
fun SettingsScreen(
    uiState: SettingsPreferences,
    onPushNotificationsChanged: (Boolean) -> Unit,
    onAccountClick: () -> Unit,
    onPasswordClick: () -> Unit,
    onAppearanceClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onPermissionsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onStorageClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLegalClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmLogout by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val notificationPermissionMissing = android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
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
                start = EkataSpacing.md,
                top = innerPadding.calculateTopPadding(),
                end = EkataSpacing.md,
                bottom = innerPadding.calculateBottomPadding() + EkataSpacing.lg,
            ),
        ) {
            item { SettingsHeader() }

            item { SettingsSectionHeading(R.string.settings_account) }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Outlined.AccountCircle,
                        titleRes = R.string.settings_account_information,
                        subtitleRes = R.string.settings_account_information_subtitle,
                        onClick = onAccountClick,
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Lock,
                        titleRes = R.string.settings_change_password,
                        subtitleRes = R.string.settings_change_password_subtitle,
                        onClick = onPasswordClick,
                    )
                }
            }

            item { SettingsSectionHeading(R.string.settings_app_preferences) }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        titleRes = R.string.settings_notifications,
                        subtitleRes = R.string.settings_notifications_subtitle,
                        checked = uiState.pushNotificationsEnabled,
                        onCheckedChange = onPushNotificationsChanged,
                    )
                    if (notificationPermissionMissing) {
                        androidx.compose.material3.TextButton(
                            onClick = { context.startActivity(android.content.Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)) },
                            modifier = Modifier.padding(start = 60.dp),
                        ) { Text(stringResource(R.string.ui_device_notifications_are_unavailable_open_settings)) }
                    }
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        titleRes = R.string.settings_appearance,
                        subtitleRes = R.string.settings_appearance_subtitle,
                        subtitle = stringResource(when(uiState.themeMode){"dark"->R.string.settings_theme_dark;"light"->R.string.settings_theme_light;else->R.string.settings_theme_system}),
                        onClick = onAppearanceClick,
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Language,
                        titleRes = R.string.settings_language,
                        subtitleRes = R.string.settings_language_subtitle,
                        subtitle = stringResource(when(uiState.selectedLanguage){"si"->R.string.language_sinhala;"ta"->R.string.language_tamil;else->R.string.language_english}),
                        onClick = onLanguageClick,
                    )
                }
            }

            item { SettingsSectionHeading(R.string.settings_travel_privacy) }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.Outlined.LocationOn,
                        titleRes = R.string.settings_location_permissions,
                        subtitleRes = R.string.settings_location_permissions_subtitle,
                        subtitle = stringResource(if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED || androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) R.string.permission_allowed else R.string.permission_off),
                        onClick = onPermissionsClick,
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.AdminPanelSettings,
                        titleRes = R.string.settings_privacy,
                        subtitleRes = R.string.settings_privacy_subtitle,
                        onClick = onPrivacyClick,
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.FolderOpen,
                        titleRes = R.string.settings_data_storage,
                        subtitleRes = R.string.settings_data_storage_subtitle,
                        onClick = onStorageClick,
                    )
                }
            }

            item { SettingsSectionHeading(R.string.settings_support) }
            item {
                SettingsGroup {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        titleRes = R.string.settings_help_support,
                        subtitleRes = R.string.settings_help_support_subtitle,
                        onClick = onHelpClick,
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Info,
                        titleRes = R.string.settings_about_ekatayan,
                        subtitleRes = R.string.settings_about_ekatayan_subtitle,
                        onClick = onAboutClick,
                    )
                    SettingsDivider()
                    SettingsRow(
                        icon = Icons.Outlined.Description,
                        titleRes = R.string.settings_terms_privacy,
                        subtitleRes = R.string.settings_terms_privacy_subtitle,
                        onClick = onLegalClick,
                    )
                }
            }

            item { SettingsSectionHeading(R.string.settings_account_actions) }
            item {
                SettingsGroup {
                    SettingsActionRow(
                        icon = Icons.AutoMirrored.Outlined.Logout,
                        titleRes = R.string.settings_logout,
                        onClick = { confirmLogout = true },
                    )
                }
            }
            item { Text(stringResource(R.string.app_version_short,com.ekatayan.app.BuildConfig.VERSION_NAME),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.fillMaxWidth().padding(top=EkataSpacing.lg),textAlign=androidx.compose.ui.text.style.TextAlign.Center) }
        }
    }
    if(confirmLogout) androidx.compose.material3.AlertDialog(onDismissRequest={confirmLogout=false},title={Text(stringResource(R.string.ui_log_out_of_ekatayan))},text={Text(stringResource(R.string.ui_you_ll_need_to_sign_in_again_to_access_your_account))},confirmButton={androidx.compose.material3.TextButton(onClick={confirmLogout=false;onLogoutClick()}){Text(stringResource(R.string.settings_logout))}},dismissButton={androidx.compose.material3.TextButton(onClick={confirmLogout=false}){Text(stringResource(R.string.planner_date_cancel))}})
}

@Composable
private fun SettingsHeader() {
    Text(
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxWidth().padding(top = HeaderActionsTopPadding),
    )
}

@Composable
private fun SettingsSectionHeading(@StringRes titleRes: Int) {
    EkataSectionHeading(
        title = stringResource(titleRes),
        modifier = Modifier.padding(top = EkataSpacing.lg, bottom = EkataSpacing.sm),
    )
}

@Composable
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low),
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    checked: Boolean? = null,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().height(SettingsRowHeight)
            .clickable(enabled = checked == null, onClick = onClick)
            .padding(horizontal = EkataSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon)
        Spacer(Modifier.width(EkataSpacing.sm))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(EkataSpacing.xxs)) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle ?: stringResource(subtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(EkataSpacing.xs))
        if (checked != null && onCheckedChange != null) {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(EkataIconSize.small),
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    @StringRes titleRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(SettingsRowHeight).clickable(onClick = onClick)
            .padding(horizontal = EkataSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(SettingsIconContainerSize),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.errorContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(EkataIconSize.medium))
            }
        }
        Spacer(Modifier.width(EkataSpacing.sm))
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector) {
    Surface(
        modifier = Modifier.size(SettingsIconContainerSize),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(EkataIconSize.medium))
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = EkataSpacing.md),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    )
}
