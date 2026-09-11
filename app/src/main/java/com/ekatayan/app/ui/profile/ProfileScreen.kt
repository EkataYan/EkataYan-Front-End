package com.ekatayan.app.ui.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.*
import com.ekatayan.app.core.designsystem.theme.*
import com.ekatayan.app.data.model.ProfileDetails
import com.ekatayan.app.viewmodel.ProfileUiState

private val ProfileMenuItemHeight = 76.dp
private val ProfileMenuIconContainerSize = 40.dp
private val ProfileAvatarSize = 88.dp

@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onRefreshErrorShown: () -> Unit,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onWishlistClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onProfileClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val profile = state.profile ?: ProfileDetails(name = "", location = "")
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshErrorMessage = stringResource(R.string.profile_refresh_error)

    LaunchedEffect(state.error) {
        if (state.error != null) {
            snackbarHostState.showSnackbar(refreshErrorMessage)
            onRefreshErrorShown()
        }
    }

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = EkataSpacing.xxl * 2 + EkataSpacing.xl),
        ) {
            item { ProfileTopBar() }
            item {
                ProfileIdentityHeader(
                    name = state.name.ifBlank { profile.name },
                    email = state.email,
                    onEditProfileClick = onEditProfileClick,
                    modifier = Modifier.padding(start = EkataSpacing.md, end = EkataSpacing.md, top = EkataSpacing.lg),
                )
            }
            if (state.isLoading) {
                item { ProfileMessageCard(R.string.profile_loading, Modifier.padding(horizontal = EkataSpacing.md, vertical = EkataSpacing.sm)) }
            }
            item { ProfileSectionHeading(R.string.profile_section_travel_activity) }
            item {
                ProfileMenuGroup(Modifier.padding(horizontal = EkataSpacing.md)) {
                    ProfileMenuItem(Icons.Outlined.FlightTakeoff, R.string.profile_menu_my_trips, R.string.profile_menu_my_trips_subtitle, onTripsClick)
                    ProfileMenuDivider()
                    ProfileMenuItem(Icons.Outlined.FavoriteBorder, R.string.profile_menu_wishlist, R.string.profile_menu_wishlist_subtitle, onWishlistClick)
                    ProfileMenuDivider()
                    ProfileMenuItem(
                        Icons.Outlined.Tune,
                        R.string.profile_menu_travel_preferences,
                        R.string.profile_menu_travel_preferences_subtitle,
                        onClick = {
                            // TODO: Navigate when a Travel Preferences destination is implemented.
                        },
                    )
                    ProfileMenuDivider()
                    ProfileMenuItem(Icons.Outlined.Groups, R.string.profile_menu_my_groups, R.string.profile_menu_my_groups_subtitle, onGroupsClick)
                    ProfileMenuDivider()
                    ProfileMenuItem(Icons.Outlined.AccountBalanceWallet, R.string.profile_menu_expense_history, R.string.profile_menu_expense_history_subtitle, onExpensesClick)
                }
            }
            item {
                ProfileMenuGroup(Modifier.padding(start = EkataSpacing.md, end = EkataSpacing.md, top = EkataSpacing.lg)) {
                    ProfileMenuItem(Icons.Outlined.Settings, R.string.profile_menu_settings, R.string.profile_menu_settings_subtitle, onSettingsClick)
                }
            }
        }

        AppBottomNavigation(
            selectedItem = AppBottomNavItem.PROFILE,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter),
            compact = true,
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = EkataSpacing.xxl + EkataSpacing.lg),
        )
    }
}

@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = EkataSpacing.md, top = HeaderActionsTopPadding, end = EkataSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.profile_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileIdentityHeader(name: String, email: String, onEditProfileClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low),
    ) {
        Row(Modifier.fillMaxWidth().padding(EkataSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.profile_avatar_placeholder),
                contentDescription = stringResource(R.string.profile_avatar_description),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(ProfileAvatarSize).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            )
            Spacer(Modifier.width(EkataSpacing.md))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(EkataSpacing.xs)) {
                Text(
                    text = name.ifBlank { stringResource(R.string.profile_unavailable) },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Email, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(EkataIconSize.small))
                    Spacer(Modifier.width(EkataSpacing.xs))
                    Text(
                        text = email.ifBlank { stringResource(R.string.profile_unavailable) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                EkataSecondaryButton(text = stringResource(R.string.profile_edit_action), onClick = onEditProfileClick)
            }
        }
    }
}

@Composable
private fun ProfileSectionHeading(@StringRes titleRes: Int) {
    EkataSectionHeading(
        title = stringResource(titleRes),
        modifier = Modifier.padding(start = EkataSpacing.md, end = EkataSpacing.md, top = EkataSpacing.lg, bottom = EkataSpacing.sm),
    )
}

@Composable
private fun ProfileMenuGroup(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low),
    ) { Column { content() } }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    @StringRes titleRes: Int,
    @StringRes subtitleRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(ProfileMenuItemHeight).clickable(onClick = onClick).padding(horizontal = EkataSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(ProfileMenuIconContainerSize),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(EkataIconSize.medium))
            }
        }
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
                text = stringResource(subtitleRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(EkataSpacing.xs))
        Icon(
            Icons.AutoMirrored.Outlined.ArrowForwardIos,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(EkataIconSize.small),
        )
    }
}

@Composable
private fun ProfileMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp, end = EkataSpacing.md),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
    )
}

@Composable
private fun ProfileMessageCard(@StringRes message: Int, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
        Text(
            stringResource(message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(EkataSpacing.md),
        )
    }
}
