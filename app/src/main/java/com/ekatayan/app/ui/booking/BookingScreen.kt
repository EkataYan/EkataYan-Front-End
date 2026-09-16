package com.ekatayan.app.ui.booking

import com.ekatayan.app.data.model.BookingCategory
import com.ekatayan.app.data.model.BookingPlace
import com.ekatayan.app.viewmodel.BookingUiState

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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.component.AppBottomNavItem
import com.ekatayan.app.core.designsystem.component.AppBottomNavigation
import com.ekatayan.app.core.designsystem.component.HeaderActions
import com.ekatayan.app.core.designsystem.component.HeaderActionsTopPadding
import com.ekatayan.app.core.designsystem.theme.EkataBackground
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataCardBackground
import com.ekatayan.app.core.designsystem.theme.EkataLightBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextPrimary
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary

private val BookingPopupBorder = Color(0xFFAEDCFA)

@Composable
private fun BookingCategory.localizedLabel(): String = stringResource(when (this) {
    BookingCategory.ALL -> R.string.filter_all
    BookingCategory.HOTELS -> R.string.booking_category_hotels
    BookingCategory.RESTAURANTS -> R.string.booking_category_restaurants
    BookingCategory.VACATION_RENTALS -> R.string.booking_category_vacation_rentals
    BookingCategory.TRANSPORTATION -> R.string.booking_category_transportation
    BookingCategory.SAFARIS -> R.string.booking_category_safaris
    BookingCategory.ACTIVITIES -> R.string.booking_category_activities
    BookingCategory.EVENTS -> R.string.booking_category_events
})
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
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    hasUnreadNotifications: Boolean,
    modifier: Modifier = Modifier,
) {
    var destinationPickerVisible by remember { mutableStateOf(false) }
    var destinationQuery by remember { mutableStateOf("") }
    var filterPopupVisible by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<BookingPlace?>(null) }

    Box(modifier.fillMaxSize().background(EkataBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = HeaderActionsTopPadding, bottom = 122.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                text = stringResource(R.string.home_booking),
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                    )
                    HeaderActions(
                        onNotificationClick = onNotificationClick,
                        onSettingsClick = onSettingsClick,
                        hasUnreadNotifications = hasUnreadNotifications,
                    )
                }
            }
            item {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(14.dp)) {
                    Text(stringResource(R.string.ui_preview_browse_travel_options_here_online_booking_is_n), modifier = Modifier.fillMaxWidth().padding(12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                    text = stringResource(R.string.booking_most_popular),
                    fontSize = 21.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            if (uiState.hasResults) {
                item {
                    if (uiState.popularPlaces.isEmpty()) {
                        SectionEmptyState(stringResource(R.string.booking_no_popular_matches))
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(end = 6.dp),
                        ) {
                            items(uiState.popularPlaces, key = BookingPlace::id) { place ->
                                PopularPlaceCard(place = place, onClick = { selectedPlace = place })
                            }
                        }
                    }
                }
                item {
                    Text(
                    text = stringResource(R.string.booking_recommended),
                        fontSize = 21.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                if (uiState.recommendedPlaces.isEmpty()) {
                    item { SectionEmptyState(stringResource(R.string.booking_no_recommended_matches)) }
                } else {
                    items(uiState.recommendedPlaces.chunked(2), key = { row -> row.first().id }) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            row.forEach { place ->
                                RecommendedPlaceCard(
                                    place = place,
                                    onClick = { selectedPlace = place },
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
            selectedItem = AppBottomNavItem.HOME,
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

    selectedPlace?.let { place ->
        BookingPlaceDialog(place = place, onDismiss = { selectedPlace = null })
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
            .heightIn(min = 48.dp)
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
            text = destination ?: stringResource(R.string.booking_where_to_go),
            color = if (destination == null) EkataTextSecondary else EkataTextPrimary,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (destination != null) {
            IconButton(onClick = onClearClick, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.ui_clear_destination),
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
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
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
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                }),
                decorationBox = { inner ->
                    Box {
                        if (query.isBlank()) {
                            Text(
                                text = stringResource(R.string.booking_search_hint),
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
                .size(48.dp)
                .background(Color.White, RoundedCornerShape(12.dp)),
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = stringResource(R.string.ui_filter),
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
                    text = category.localizedLabel(),
                    color = EkataTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun PopularPlaceCard(place: BookingPlace, onClick: () -> Unit, modifier: Modifier = Modifier) {
    BookingImageCard(
        place = place,
        modifier = modifier.width(244.dp).height(120.dp),
        showLocation = true,
        onClick = onClick,
    )
}

@Composable
private fun RecommendedPlaceCard(place: BookingPlace, onClick: () -> Unit, modifier: Modifier = Modifier) {
    BookingImageCard(
        place = place,
        modifier = modifier.height(108.dp),
        showLocation = false,
        onClick = onClick,
    )
}

@Composable
private fun BookingImageCard(
    place: BookingPlace,
    modifier: Modifier = Modifier,
    showLocation: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
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
            text = stringResource(R.string.booking_no_places_found),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = EkataTextPrimary,
        )
        Text(
            text = stringResource(R.string.booking_try_different_filters),
            color = EkataTextSecondary,
            fontSize = 13.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SmallActionButton(text = stringResource(R.string.booking_reset_all), onClick = onResetFilters)
            SmallActionButton(text = stringResource(R.string.booking_clear_search), onClick = onClearSearch)
            SmallActionButton(text = stringResource(R.string.booking_clear_destination), onClick = onClearDestination)
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
                text = stringResource(R.string.booking_choose_destination),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = EkataTextPrimary,
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.home_search_action)) },
                colors = bookingPopupTextFieldColors(),
                shape = RoundedCornerShape(14.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedDestination?.let { stringResource(R.string.booking_selected_destination, it) } ?: stringResource(R.string.booking_all_destinations),
                    color = EkataTextPrimary,
                    fontSize = 13.sp,
                )
                    TextButtonLike(text = stringResource(R.string.booking_clear), onClick = onClearDestination)
            }
            Divider(color = BookingPopupBorder)
            if (filteredDestinations.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_destinations_found),
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
                text = stringResource(R.string.booking_filters),
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = EkataTextPrimary,
            )
            Text(
                text = stringResource(R.string.booking_destination_filter, selectedDestination ?: stringResource(R.string.filter_all)),
                color = EkataTextPrimary,
                fontSize = 13.sp,
            )
            Text(
                text = stringResource(R.string.booking_category_filter, selectedCategory.localizedLabel()),
                color = EkataTextPrimary,
                fontSize = 13.sp,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(text = stringResource(R.string.booking_clear_search), onClick = onClearSearch)
                SmallActionButton(text = stringResource(R.string.booking_clear_destination), onClick = onClearDestination)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallActionButton(text = stringResource(R.string.booking_reset_all), onClick = onResetFilters)
            TextButtonLike(text = stringResource(R.string.booking_close), onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun BookingPlaceDialog(place: BookingPlace, onDismiss: () -> Unit) {
    BookingPopupSurface(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = place.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = EkataTextPrimary,
            )
            Text(
                text = stringResource(R.string.booking_place_location, place.location),
                color = EkataTextSecondary,
                fontSize = 13.sp,
            )
            Text(
                text = stringResource(R.string.booking_place_category, place.category.badgeLabel),
                color = EkataTextSecondary,
                fontSize = 13.sp,
            )
            Text(
                text = stringResource(R.string.booking_unavailable_message),
                color = EkataTextPrimary,
                fontSize = 13.sp,
            )
            TextButtonLike(
                text = stringResource(R.string.booking_close),
                onClick = onDismiss,
            )
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
