package com.ekatayan.app.feature.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataCardBackground
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary

private val BookingPopupBorder = Color(0xFFAEDCFA)
private val BookingPopupShape = RoundedCornerShape(22.dp)

@Composable
fun BookingScreen(
    uiState: BookingUiState,
    onSearchQueryChange: (String) -> Unit,
    onDestinationSelected: (String?) -> Unit,
    onCategorySelected: (BookingCategory) -> Unit,
    onClearDestination: () -> Unit,
    onClearSearch: () -> Unit,
    onResetFilters: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var destinationPickerVisible by remember { mutableStateOf(false) }
    var destinationQuery by remember { mutableStateOf("") }
    var filterPopupVisible by remember { mutableStateOf(false) }

    Box(modifier.fillMaxSize().background(EkataBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 122.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Booking",
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    HeaderActions(
                        onNotificationClick = {},
                        onSettingsClick = {},
                    )
                }
            }
            item {
                DestinationField(
                    destination = uiState.selectedDestination,
                    onClick = {
                        destinationQuery = uiState.selectedDestination.orEmpty()
                        destinationPickerVisible = true
                    },
                    onClearClick = onClearDestination,
                )
            }
            item {
                SearchRow(
                    query = uiState.searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onFilterClick = { filterPopupVisible = true },
                )
            }
            item {
                CategoryChips(
                    categories = BookingCategory.entries,
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = onCategorySelected,
                )
            }
            item {
                Text(
                    text = "Most Popular",
                    fontSize = 21.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            if (uiState.hasResults) {
                item {
                    if (uiState.popularPlaces.isEmpty()) {
                        SectionEmptyState("No popular places match the current filters.")
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 6.dp),
                        ) {
                            items(uiState.popularPlaces, key = BookingPlace::id) { place ->
                                PopularPlaceCard(place = place)
                            }
                        }
                    }
                }
                item {
                    Text(
                        text = "Recommended",
                        fontSize = 21.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (uiState.recommendedPlaces.isEmpty()) {
                    item { SectionEmptyState("No recommended places match the current filters.") }
                } else {
                    items(uiState.recommendedPlaces.chunked(2), key = { row -> row.first().id }) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            row.forEach { place ->
                                RecommendedPlaceCard(
                                    place = place,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                item {
                    EmptyBookingState(
                        onResetFilters = onResetFilters,
                        onClearSearch = onClearSearch,
                        onClearDestination = onClearDestination,
                    )
                }
            }
        }

        AppBottomNavigation(
            selectedItem = AppBottomNavItem.TRIPS,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (destinationPickerVisible) {
        DestinationPickerDialog(
            destinations = uiState.availableDestinations,
            selectedDestination = uiState.selectedDestination,
            searchQuery = destinationQuery,
            onSearchQueryChange = { destinationQuery = it },
            onDismiss = { destinationPickerVisible = false },
            onDestinationSelected = {
                onDestinationSelected(it)
                destinationPickerVisible = false
            },
            onClearDestination = {
                onClearDestination()
                destinationPickerVisible = false
            },
        )
    }

    if (filterPopupVisible) {
        BookingFilterDialog(
            selectedDestination = uiState.selectedDestination,
            selectedCategory = uiState.selectedCategory,
            onDismiss = { filterPopupVisible = false },
            onClearSearch = onClearSearch,
            onClearDestination = onClearDestination,
            onResetFilters = {
                onResetFilters()
                filterPopupVisible = false
            },
        )
    }
}

@Composable
private fun DestinationField(
    destination: String?,
    onClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .shadow(0.dp, RoundedCornerShape(999.dp))
            .background(Color.White, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 10.dp)
                .size(26.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = EkataTextPrimary,
                modifier = Modifier.size(23.dp),
            )
        }
        Text(
            text = destination ?: "Where to go?",
            color = if (destination == null) EkataTextSecondary else EkataTextPrimary,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (destination != null) {
            IconButton(onClick = onClearClick, modifier = Modifier.size(34.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear destination",
                    tint = EkataTextPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Spacer(Modifier.width(10.dp))
        }
    }
}

@Composable
private fun SearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .shadow(0.dp, RoundedCornerShape(999.dp))
                .background(Color.White, RoundedCornerShape(999.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = EkataBlue,
                modifier = Modifier.size(23.dp),
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(color = EkataTextPrimary, fontSize = 14.sp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {}),
                decorationBox = { inner ->
                    Box {
                        if (query.isBlank()) {
                            Text(
                                text = "Places, Hotels, Activities and more",
                                color = EkataTextSecondary,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        inner()
                    }
                },
            )
        }
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(40.dp)
                .background(Color.White, RoundedCornerShape(12.dp)),
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = EkataTextPrimary,
            )
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<BookingCategory>,
    selectedCategory: BookingCategory,
    onCategorySelected: (BookingCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(end = 6.dp),
    ) {
        items(categories, key = BookingCategory::name) { category ->
            val selected = category == selectedCategory
            Surface(
                onClick = { onCategorySelected(category) },
                shape = RoundedCornerShape(999.dp),
                color = if (selected) EkataLightBlue else Color.White,
                border = BorderStroke(1.dp, if (selected) EkataLightBlue else EkataLightBlue),
                shadowElevation = if (selected) 4.dp else 1.dp,
            ) {
                Text(
                    text = category.chipLabel,
                    color = EkataTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun PopularPlaceCard(place: BookingPlace, modifier: Modifier = Modifier) {
    BookingImageCard(
        place = place,
        modifier = modifier.width(244.dp).height(120.dp),
        showLocation = true,
    )
}

@Composable
private fun RecommendedPlaceCard(place: BookingPlace, modifier: Modifier = Modifier) {
    BookingImageCard(
        place = place,
        modifier = modifier.height(108.dp),
        showLocation = false,
    )
}

@Composable
private fun BookingImageCard(
    place: BookingPlace,
    modifier: Modifier = Modifier,
    showLocation: Boolean,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = {})
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(place.imageRes),
            contentDescription = place.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xAA000000)),
                        startY = 85f,
                    ),
                ),
        )
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            shape = RoundedCornerShape(999.dp),
            color = place.category.chipColor,
        ) {
            Text(
                text = place.category.badgeLabel,
                color = EkataTextPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
        ) {
            Text(
                text = place.name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (showLocation) {
                Text(
                    text = place.location,
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun SectionEmptyState(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = EkataCardBackground,
        border = BorderStroke(1.dp, EkataLightBlue),
    ) {
        Text(
            text = message,
            color = EkataTextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
private fun EmptyBookingState(
    onResetFilters: () -> Unit,
    onClearSearch: () -> Unit,
    onClearDestination: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "No places found",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = EkataTextPrimary,
        )
        Text(
            text = "Try a different search, destination, or category.",
            color = EkataTextSecondary,
            fontSize = 13.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton(text = "Reset all", onClick = onResetFilters)
            SmallActionButton(text = "Clear search", onClick = onClearSearch)
            SmallActionButton(text = "Clear destination", onClick = onClearDestination)
        }
    }
}

@Composable
private fun SmallActionButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BookingPopupBorder),
    ) {
        Text(
            text = text,
            color = EkataTextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun DestinationPickerDialog(
    destinations: List<String>,
    selectedDestination: String?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onDestinationSelected: (String) -> Unit,
    onClearDestination: () -> Unit,
) {
    val filteredDestinations = remember(searchQuery, destinations) {
        val normalized = searchQuery.trim().lowercase()
        destinations.filter { destination ->
            normalized.isBlank() || destination.lowercase().contains(normalized)
        }
    }

    BookingPopupSurface(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Choose destination",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = EkataTextPrimary,
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search destinations") },
                colors = bookingPopupTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedDestination?.let { "Selected: $it" } ?: "All destinations",
                    color = EkataTextPrimary,
                    fontSize = 13.sp,
                )
                TextButtonLike(text = "Clear", onClick = onClearDestination)
            }
            Divider(color = BookingPopupBorder)
            if (filteredDestinations.isEmpty()) {
                Text(
                    text = "No destinations found",
                    color = EkataTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(vertical = 14.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filteredDestinations, key = { it }) { destination ->
                        Surface(
                            onClick = { onDestinationSelected(destination) },
                            shape = RoundedCornerShape(14.dp),
                            color = if (destination.equals(selectedDestination, ignoreCase = true)) EkataLightBlue else Color.White,
                            border = BorderStroke(1.dp, BookingPopupBorder),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = EkataTextPrimary,
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = destination,
                                    color = EkataTextPrimary,
                                    fontSize = 14.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BookingFilterDialog(
    selectedDestination: String?,
    selectedCategory: BookingCategory,
    onDismiss: () -> Unit,
    onClearSearch: () -> Unit,
    onClearDestination: () -> Unit,
    onResetFilters: () -> Unit,
) {
    BookingPopupSurface(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Filters",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = EkataTextPrimary,
            )
            Text(
                text = "Destination: ${selectedDestination ?: "All"}",
                color = EkataTextPrimary,
                fontSize = 13.sp,
            )
            Text(
                text = "Category: ${selectedCategory.chipLabel}",
                color = EkataTextPrimary,
                fontSize = 13.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(text = "Clear search", onClick = onClearSearch)
                SmallActionButton(text = "Clear destination", onClick = onClearDestination)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(text = "Reset all", onClick = onResetFilters)
                TextButtonLike(text = "Close", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun BookingPopupSurface(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = BookingPopupShape,
            color = Color.White,
            contentColor = EkataTextPrimary,
            border = BorderStroke(1.dp, BookingPopupBorder),
            shadowElevation = 8.dp,
        ) {
            content()
        }
    }
}

@Composable
private fun TextButtonLike(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = Color.White,
        border = BorderStroke(1.dp, BookingPopupBorder),
    ) {
        Text(
            text = text,
            color = EkataTextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun bookingPopupTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = EkataTextPrimary,
    unfocusedTextColor = EkataTextPrimary,
    disabledTextColor = EkataTextPrimary,
    cursorColor = EkataTextPrimary,
    focusedBorderColor = BookingPopupBorder,
    unfocusedBorderColor = BookingPopupBorder,
    focusedLabelColor = EkataTextPrimary,
    unfocusedLabelColor = EkataTextPrimary,
    focusedPlaceholderColor = EkataTextSecondary,
    unfocusedPlaceholderColor = EkataTextSecondary,
)
