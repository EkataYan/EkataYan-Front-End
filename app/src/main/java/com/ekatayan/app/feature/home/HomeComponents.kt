package com.ekatayan.app.feature.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataCardBackground
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataNavigationBackground
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding

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
    Box(Modifier.fillMaxWidth().height(262.dp)) {
        Image(
            painter = painterResource(R.drawable.home_header),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(232.dp).clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp)),
        )
        Column(Modifier.padding(start = 21.dp, top = 55.dp)) {
            Text(
                text = "Welcome, ${user.name}",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 27.sp,
                color = EkataTextPrimary,
            )
            Text("Where shall we explore today?", fontSize = 13.sp, color = EkataTextPrimary)
        }
        HeaderActions(
            onNotificationClick = onNotificationClick,
            hasUnreadNotifications = hasUnreadNotifications,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = HeaderActionsTopPadding, end = 14.dp),
        )
        HomeSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            onSearchSubmit = onSearchSubmit,
            modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 22.dp),
        )
    }
}

@Composable
fun HomeSearchBar(query: String, onQueryChange: (String) -> Unit, onSearchSubmit: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().height(55.dp).shadow(8.dp, CircleShape).background(Color.White, CircleShape).padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = EkataBlue)
        Spacer(Modifier.width(11.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = EkataTextPrimary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchSubmit() }),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) Text("Search Your Destination", fontSize = 14.sp, color = EkataTextSecondary)
                    inner()
                }
            },
        )
    }
}

@Composable
fun QuickActions(onMapsClick: () -> Unit, onWishlistClick: () -> Unit, onBookingClick: () -> Unit, onGroupHubClick: () -> Unit, onPartnershipClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 17.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuickActionItem("Maps", Icons.Default.Map, onMapsClick)
        QuickActionItem("Wishlist", Icons.Outlined.FavoriteBorder, onWishlistClick)
        QuickActionItem("Booking", Icons.Default.WorkOutline, onBookingClick)
        QuickActionItem("Group Hub", Icons.Default.Groups, onGroupHubClick)
        QuickActionItem(stringResource(R.string.bp_partnership), Icons.Default.Storefront, onPartnershipClick)
    }
}

@Composable
fun QuickActionItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier.widthIn(min = 70.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(49.dp).background(EkataLightBlue, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, label, tint = EkataTextPrimary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
fun RecommendedSection(destinations: List<RecommendedDestination>, onDestinationClick: (Int) -> Unit) {
    if (destinations.isEmpty()) {
        EmptyMessage("Recommendations will appear here")
        return
    }
    val pagerState = rememberPagerState(pageCount = { destinations.size })
    HorizontalPager(
        state = pagerState,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        pageSpacing = 12.dp,
        modifier = Modifier.fillMaxWidth(),
    ) { page ->
        RecommendedDestinationCard(
            destination = destinations[page],
            page = page,
            count = destinations.size,
            selectedPage = pagerState.currentPage,
            onClick = { onDestinationClick(destinations[page].id) },
        )
    }
}

@Composable
fun RecommendedDestinationCard(destination: RecommendedDestination, page: Int, count: Int, selectedPage: Int, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(1.9f).clip(RoundedCornerShape(24.dp)).clickable(onClick = onClick),
    ) {
        Image(painterResource(destination.imageRes), destination.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xD9000000)), startY = 100f)))
        Column(Modifier.align(Alignment.BottomStart).padding(start = 18.dp, end = 92.dp, bottom = 16.dp)) {
            Text(destination.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(destination.description, color = Color.White.copy(alpha = .9f), fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        PageIndicator(count, selectedPage, Modifier.align(Alignment.BottomEnd).padding(18.dp))
    }
}

@Composable
fun PageIndicator(count: Int, selectedPage: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(count) { index ->
            Box(Modifier.size(if (index == selectedPage) 8.dp else 6.dp).background(if (index == selectedPage) Color.White else Color.White.copy(alpha = .45f), CircleShape))
        }
    }
}

@Composable
fun HomeInfoCards(trip: UpcomingTrip?, weather: WeatherInfo?, onUpcomingTripClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        UpcomingTripCard(trip, onUpcomingTripClick, Modifier.weight(1f))
        WeatherCard(weather, Modifier.weight(1f))
    }
}

@Composable
fun UpcomingTripCard(trip: UpcomingTrip?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    InfoCard(modifier.clickable(onClick = onClick)) {
        Text("Upcoming Trip", color = EkataBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        if (trip == null) {
            Text("No upcoming trips", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Plan a Trip", color = EkataBlue, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        } else {
            Text(trip.destination, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            InfoLine(Icons.Default.CalendarMonth, trip.date)
            InfoLine(Icons.Outlined.Schedule, trip.duration)
            Text("View  >", color = EkataBlue, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
            Image(painterResource(trip.imageRes), null, Modifier.align(Alignment.End).size(54.dp).offset(y = (-27).dp).clip(RoundedCornerShape(13.dp)), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherInfo?, modifier: Modifier = Modifier) {
    InfoCard(modifier) {
        if (weather == null) {
            Text("Weather", color = EkataBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Text("Weather unavailable", fontSize = 13.sp, color = EkataTextSecondary)
        } else {
            Text("Weather in ${weather.location}", color = EkataBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${weather.temperature}°C", fontSize = 27.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Icon(weatherIcon(weather.weatherType), weather.condition, tint = EkataBlue, modifier = Modifier.size(27.dp))
            }
            Text(weather.condition, fontSize = 12.sp, color = EkataTextSecondary)
            Text("Humidity  ${weather.humidity}%", fontSize = 11.sp, color = EkataTextSecondary, modifier = Modifier.padding(top = 5.dp))
            weather.imageRes?.let {
                Image(painterResource(it), null, Modifier.align(Alignment.End).size(54.dp).offset(y = (-18).dp).clip(RoundedCornerShape(13.dp)), contentScale = ContentScale.Crop)
            }
        }
    }
}

private fun weatherIcon(type: WeatherType): ImageVector = when (type) {
    WeatherType.SUNNY -> Icons.Default.WbSunny
    WeatherType.CLOUDY -> Icons.Default.Cloud
    WeatherType.RAINY, WeatherType.STORMY -> Icons.Default.Thunderstorm
}

@Composable
private fun InfoCard(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.height(166.dp),
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = EkataCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) { Column(Modifier.fillMaxSize().padding(14.dp), content = content) }
}

@Composable
private fun InfoLine(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 5.dp)) {
        Icon(icon, null, tint = EkataTextSecondary, modifier = Modifier.size(13.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, fontSize = 10.sp, color = EkataTextSecondary)
    }
}

@Composable
fun PopularDestinationsSection(destinations: List<PopularDestination>, onDestinationClick: (Int) -> Unit) {
    if (destinations.isEmpty()) {
        EmptyMessage("Popular destinations will appear here")
        return
    }
    LazyRow(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
    ) {
        items(destinations, key = { it.id }) { destination ->
            DestinationImageCard(destination) { onDestinationClick(destination.id) }
        }
    }
}

@Composable
fun DestinationImageCard(destination: PopularDestination, onClick: () -> Unit) {
    Box(Modifier.width(132.dp).height(104.dp).clip(RoundedCornerShape(19.dp)).clickable(onClick = onClick)) {
        Image(painterResource(destination.imageRes), destination.name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB8000000)))))
        Text(destination.name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, modifier = Modifier.align(Alignment.BottomStart).padding(10.dp))
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(Modifier.fillMaxWidth().height(100.dp).padding(horizontal = 20.dp).background(EkataCardBackground, RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 13.sp, color = EkataTextSecondary)
    }
}
