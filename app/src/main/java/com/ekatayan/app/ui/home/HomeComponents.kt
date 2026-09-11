package com.ekatayan.app.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.EkataEmptyState
import com.ekatayan.app.core.designsystem.component.EkataImageSupportingText
import com.ekatayan.app.core.designsystem.component.EkataImageCard
import com.ekatayan.app.core.designsystem.component.EkataImageTitle
import com.ekatayan.app.core.designsystem.component.EkataPageIndicator
import com.ekatayan.app.core.designsystem.component.EkataQuickActionCard
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding
import com.ekatayan.app.core.designsystem.theme.EkataComponentSize
import com.ekatayan.app.core.designsystem.theme.EkataElevation
import com.ekatayan.app.core.designsystem.theme.EkataIconSize
import com.ekatayan.app.core.designsystem.theme.EkataImageScrim
import com.ekatayan.app.core.designsystem.theme.EkataOnImage
import com.ekatayan.app.core.designsystem.theme.EkataRadius
import com.ekatayan.app.core.designsystem.theme.EkataSpacing
import com.ekatayan.app.core.designsystem.theme.EkataSuccess
import com.ekatayan.app.core.designsystem.theme.EkataWarning
import com.ekatayan.app.data.model.PopularDestination
import com.ekatayan.app.data.model.RecommendedDestination
import com.ekatayan.app.data.model.UpcomingTrip
import com.ekatayan.app.data.model.User
import com.ekatayan.app.data.model.WeatherInfo
import com.ekatayan.app.data.model.WeatherType

@Composable
fun HeroSection(
    user: User,
    searchQuery: String,
    onNotificationClick: () -> Unit,
    hasUnreadNotifications: Boolean,
    onSettingsClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchSubmit: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().height(286.dp)) {
        val heroShape = RoundedCornerShape(bottomStart = EkataRadius.extraLarge, bottomEnd = EkataRadius.extraLarge)
        Image(
            painter = painterResource(R.drawable.home_header),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(254.dp).clip(heroShape),
        )
        Box(
            Modifier.fillMaxWidth().height(254.dp).clip(heroShape).background(
                Brush.verticalGradient(
                    listOf(EkataImageScrim.copy(alpha = 0.22f), Color.Transparent, EkataImageScrim.copy(alpha = 0.38f)),
                ),
            ),
        )
        Column(
            Modifier.align(Alignment.TopStart).padding(start = EkataSpacing.md, top = 64.dp, end = 104.dp),
            verticalArrangement = Arrangement.spacedBy(EkataSpacing.xxs),
        ) {
            EkataImageTitle(stringResource(R.string.home_welcome, user.name))
            EkataImageSupportingText(stringResource(R.string.home_prompt))
        }
        HeaderActions(
            onNotificationClick = onNotificationClick,
            hasUnreadNotifications = hasUnreadNotifications,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = HeaderActionsTopPadding, end = EkataSpacing.sm)
                .clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .padding(horizontal = EkataSpacing.xxs),
        )
        HomeSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearchSubmit = onSearchSubmit,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = EkataSpacing.md),
        )
    }
}

@Composable
fun HomeSearchBar(query: String, onQueryChange: (String) -> Unit, onSearchSubmit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(EkataComponentSize.inputMinHeight)
            .shadow(EkataElevation.low, CircleShape).background(MaterialTheme.colorScheme.surface, CircleShape)
            .padding(horizontal = EkataSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, stringResource(R.string.home_search_action), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(EkataIconSize.medium))
        Spacer(Modifier.width(EkataSpacing.sm))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) Text(stringResource(R.string.home_search_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    inner()
                }
            },
        )
    }
}

@Composable
fun QuickActions(onMapsClick: () -> Unit, onWishlistClick: () -> Unit, onBookingClick: () -> Unit, onGroupHubClick: () -> Unit, onPartnershipClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(
            start = EkataSpacing.md,
            end = EkataSpacing.md,
            top = EkataSpacing.lg,
            bottom = EkataSpacing.sm,
        ),
        horizontalArrangement = Arrangement.spacedBy(EkataSpacing.xxs),
    ) {
        QuickActionItem(stringResource(R.string.home_maps), R.drawable.home_shortcut_maps, onMapsClick, Modifier.weight(1f))
        QuickActionItem(stringResource(R.string.home_wishlist), R.drawable.home_shortcut_wishlist, onWishlistClick, Modifier.weight(1f))
        QuickActionItem(stringResource(R.string.home_booking), R.drawable.home_shortcut_booking, onBookingClick, Modifier.weight(1f))
        QuickActionItem(stringResource(R.string.home_group_hub), R.drawable.home_shortcut_group_hub, onGroupHubClick, Modifier.weight(1f))
        QuickActionItem(stringResource(R.string.bp_partnership), R.drawable.home_shortcut_partnership, onPartnershipClick, Modifier.weight(1f))
    }
}

@Composable
fun QuickActionItem(
    label: String,
    @DrawableRes artworkRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EkataQuickActionCard(label = label, artworkRes = artworkRes, onClick = onClick, modifier = modifier)
}

@Composable
fun RecommendedSection(destinations: List<RecommendedDestination>, onDestinationClick: (Int) -> Unit) {
    if (destinations.isEmpty()) {
        EkataEmptyState(
            title = stringResource(R.string.home_no_recommendations_title),
            message = stringResource(R.string.home_no_recommendations_message),
            modifier = Modifier.padding(horizontal = EkataSpacing.md),
        )
        return
    }
    val pagerState = rememberPagerState(pageCount = { destinations.size })
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = EkataSpacing.md),
        pageSpacing = EkataSpacing.sm,
        modifier = Modifier.fillMaxWidth(),
    ) { page ->
        RecommendedDestinationCard(destinations[page], page, destinations.size, pagerState.currentPage) {
            onDestinationClick(destinations[page].id)
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun RecommendedDestinationCard(destination: RecommendedDestination, page: Int, count: Int, selectedPage: Int, onClick: () -> Unit) {
    EkataImageCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().aspectRatio(1.72f),
    ) {
        Image(painterResource(destination.imageRes), destination.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, EkataImageScrim.copy(alpha = 0.24f), EkataImageScrim.copy(alpha = 0.9f)),
                    startY = 40f,
                ),
            ),
        )
        Column(
            Modifier.align(Alignment.BottomStart).padding(EkataSpacing.md).padding(end = 72.dp),
            verticalArrangement = Arrangement.spacedBy(EkataSpacing.xxs),
        ) {
            Text(destination.name, color = EkataOnImage, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(destination.description, color = EkataOnImage.copy(alpha = 0.9f), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        PageIndicator(count, selectedPage, Modifier.align(Alignment.BottomEnd).padding(EkataSpacing.md))
    }
}

@Composable
fun PageIndicator(count: Int, selectedPage: Int, modifier: Modifier = Modifier) {
    EkataPageIndicator(count = count, selectedPage = selectedPage, modifier = modifier)
}

@Composable
fun HomeInfoCards(trip: UpcomingTrip?, weather: WeatherInfo?, onUpcomingTripClick: () -> Unit, weatherLoading: Boolean = false, weatherError: String? = null) {
    BoxWithConstraints(
        Modifier.fillMaxWidth().padding(
            start = EkataSpacing.md,
            end = EkataSpacing.md,
            top = EkataSpacing.lg,
            bottom = EkataSpacing.sm,
        ),
    ) {
        if (maxWidth < 360.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(EkataSpacing.sm)) {
                UpcomingTripCard(trip, onUpcomingTripClick, Modifier.fillMaxWidth().height(HomeWidgetHeight))
                WeatherCard(weather, Modifier.fillMaxWidth().height(HomeWidgetHeight), weatherLoading, weatherError)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(EkataSpacing.sm),
            ) {
                UpcomingTripCard(trip, onUpcomingTripClick, Modifier.weight(1f).height(HomeWidgetHeight))
                WeatherCard(weather, Modifier.weight(1f).height(HomeWidgetHeight), weatherLoading, weatherError)
            }
        }
    }
}

private val HomeWidgetHeight = 264.dp

@Composable
fun UpcomingTripCard(trip: UpcomingTrip?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low),
    ) {
        Column(Modifier.fillMaxSize().padding(EkataSpacing.sm)) {
            HomeWidgetHeader(
                icon = Icons.Default.Flight,
                title = stringResource(R.string.home_upcoming_trip),
                subtitle = stringResource(R.string.home_upcoming_trip_subtitle),
            )
            if (trip == null) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(stringResource(R.string.home_no_upcoming_trips), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.home_plan_trip), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Spacer(Modifier.height(EkataSpacing.xs))
                HomeInfoImage(trip.imageRes)
                Spacer(Modifier.height(EkataSpacing.xs))
                Text(trip.destination, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.fillMaxWidth().padding(top = EkataSpacing.xxs), horizontalArrangement = Arrangement.spacedBy(EkataSpacing.xs)) {
                    WidgetMetric(Icons.Default.CalendarMonth, trip.date, stringResource(R.string.home_travel_date), Modifier.weight(1f))
                    WidgetMetric(Icons.Outlined.Schedule, trip.duration, stringResource(R.string.home_duration), Modifier.weight(1f))
                }
                Spacer(Modifier.weight(1f))
                Surface(
                    onClick = onClick,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(horizontal = EkataSpacing.sm, vertical = EkataSpacing.xs), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.home_view_trip), color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(EkataSpacing.xs))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(EkataIconSize.small))
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherInfo?, modifier: Modifier = Modifier, isLoading: Boolean = false, errorMessage: String? = null) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.large, elevation = CardDefaults.cardElevation(defaultElevation = EkataElevation.low)) {
        Box(Modifier.fillMaxSize()) {
            weather?.imageRes?.let {
                Image(painterResource(it), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.78f), Color.Transparent, EkataImageScrim.copy(alpha = 0.28f)))))
            Column(Modifier.fillMaxSize().padding(EkataSpacing.sm)) {
                HomeWidgetHeader(
                    icon = Icons.Default.Cloud,
                    title = weather?.let { stringResource(R.string.home_weather_in, it.location) } ?: stringResource(R.string.home_weather),
                    subtitle = stringResource(R.string.home_weather_subtitle),
                    onImage = true,
                )
                if (isLoading) {
                    CircularProgressIndicator(color = EkataOnImage, modifier = Modifier.padding(top = EkataSpacing.lg).size(28.dp))
                } else if (weather == null) {
                    Text(errorMessage ?: stringResource(R.string.home_weather_unavailable), style = MaterialTheme.typography.bodyMedium, color = EkataOnImage, modifier = Modifier.padding(top = EkataSpacing.lg))
                } else {
                    Row(Modifier.padding(top = EkataSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.home_temperature, weather.temperature), style = MaterialTheme.typography.displaySmall, color = EkataOnImage)
                        Spacer(Modifier.width(EkataSpacing.xs))
                        Icon(weatherIcon(weather.weatherType), weather.condition, tint = EkataOnImage, modifier = Modifier.size(EkataIconSize.large))
                    }
                    Text(weather.condition, style = MaterialTheme.typography.titleMedium, color = EkataOnImage)
                    Spacer(Modifier.weight(1f))
                    Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f), shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(horizontal = EkataSpacing.xxs, vertical = EkataSpacing.xs), verticalAlignment = Alignment.CenterVertically) {
                            WeatherMetric(Icons.Default.WaterDrop, stringResource(R.string.home_humidity_value, weather.humidity), stringResource(R.string.home_humidity_label), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                            WeatherMetricDivider()
                            WeatherMetric(Icons.Default.Air, weather.windKph?.let { "${it.toInt()} km/h" } ?: "—", stringResource(R.string.home_wind_label), EkataSuccess, Modifier.weight(1f))
                            WeatherMetricDivider()
                            WeatherMetric(Icons.Default.WbTwilight, weather.sunrise ?: "—", stringResource(R.string.home_sunrise_label), EkataWarning, Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherMetric(icon: ImageVector, value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(28.dp).background(accent.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(EkataIconSize.small))
        }
        Text(value, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WeatherMetricDivider() {
    Box(Modifier.width(1.dp).height(44.dp).background(MaterialTheme.colorScheme.outline))
}

@Composable
private fun HomeWidgetHeader(icon: ImageVector, title: String, subtitle: String, onImage: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(40.dp).background(
                if (onImage) EkataOnImage.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.shapes.medium,
            ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = if (onImage) EkataOnImage else MaterialTheme.colorScheme.primary, modifier = Modifier.size(EkataIconSize.medium))
        }
        Spacer(Modifier.width(EkataSpacing.xs))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = if (onImage) EkataOnImage else MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = if (onImage) EkataOnImage.copy(alpha = 0.78f) else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun WidgetMetric(icon: ImageVector, value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(EkataIconSize.small))
            Spacer(Modifier.width(EkataSpacing.xxs))
            Text(value, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun weatherIcon(type: WeatherType): ImageVector = when (type) {
    WeatherType.SUNNY -> Icons.Default.WbSunny
    WeatherType.CLOUDY -> Icons.Default.Cloud
    WeatherType.RAINY, WeatherType.STORMY -> Icons.Default.Thunderstorm
}

@Composable
private fun HomeInfoImage(@DrawableRes imageRes: Int) {
    Image(
        painter = painterResource(imageRes),
        contentDescription = null,
        modifier = Modifier.fillMaxWidth().height(EkataComponentSize.cardMediaHeight).clip(MaterialTheme.shapes.medium),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun PopularDestinationsSection(destinations: List<PopularDestination>, onDestinationClick: (Int) -> Unit) {
    if (destinations.isEmpty()) {
        EkataEmptyState(
            title = stringResource(R.string.home_no_popular_title),
            message = stringResource(R.string.home_no_popular_message),
            modifier = Modifier.padding(horizontal = EkataSpacing.md),
        )
        return
    }
    LazyRow(contentPadding = PaddingValues(horizontal = EkataSpacing.md), horizontalArrangement = Arrangement.spacedBy(EkataSpacing.sm)) {
        items(destinations, key = { it.id }) { destination -> DestinationImageCard(destination) { onDestinationClick(destination.id) } }
    }
}

@Composable
fun DestinationImageCard(destination: PopularDestination, onClick: () -> Unit) {
    EkataImageCard(
        onClick = onClick,
        modifier = Modifier.width(164.dp).height(132.dp),
    ) {
        Image(painterResource(destination.imageRes), destination.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, EkataImageScrim.copy(alpha = 0.76f)))))
        Text(destination.name, color = EkataOnImage, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.align(Alignment.BottomStart).padding(EkataSpacing.sm))
    }
}
