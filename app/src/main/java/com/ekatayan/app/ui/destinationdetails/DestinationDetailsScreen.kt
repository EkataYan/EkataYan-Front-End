package com.ekatayan.app.ui.destinationdetails

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.data.model.Attraction
import com.ekatayan.app.data.model.AttractionHighlight
import com.ekatayan.app.data.model.DestinationDetails
import com.ekatayan.app.data.model.WishlistGroup
import com.ekatayan.app.data.model.WishlistItem
import com.ekatayan.app.ui.wishlist.WishlistGroupSelector

private val DetailBackground = Color(0xFFF9FBFE)
private val DetailText = Color(0xFF151B2B)
private val DetailSecondaryText = Color(0xFF637087)
private val DetailPaleBlue = Color(0xFFF0F7FE)
private val DetailBlue = Color(0xFF318DE5)
private val DetailOutline = Color(0xFFDCE8F4)
private val DetailShape = RoundedCornerShape(18.dp)

@Composable
fun DestinationDetailsScreen(
    destination: DestinationDetails?,
    popularPlaces: List<Attraction>,
    wishlistItem: WishlistItem?,
    wishlistGroups: List<WishlistGroup>,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onPlaceClick: (String) -> Unit,
    onGroupSelectionChange: (Int, WishlistItem, Boolean) -> Unit,
    onCreateGroupWithPlace: (String, WishlistItem) -> Boolean,
    modifier: Modifier = Modifier,
) {
    if (destination == null) {
        MissingDetails(stringResource(R.string.destination_details_not_found), onBackClick, modifier)
        return
    }
    var selectorVisible by remember(wishlistItem?.id) { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.fillMaxSize().background(DetailBackground),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            DetailsHero(
                name = destination.name,
                location = destination.location,
                imageRes = destination.imageRes,
                categories = destination.categories,
                onBackClick = onBackClick,
                onShareClick = onShareClick,
            )
        }
        item {
            AboutCard(destination.name, destination.description, Modifier.padding(horizontal = 16.dp, vertical = 14.dp))
        }
        if (popularPlaces.isNotEmpty()) {
            item {
                SectionTitle(
                    stringResource(R.string.destination_details_popular_places, destination.name),
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp),
                )
            }
            items(popularPlaces, key = Attraction::id) { place ->
                PopularPlaceRow(place, onClick = { onPlaceClick(place.id) }, Modifier.padding(horizontal = 16.dp, vertical = 5.dp))
            }
        }
        item {
            AddToWishlistButton(
                enabled = wishlistItem != null,
                onClick = { selectorVisible = true },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp),
            )
        }
        item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
    if (selectorVisible && wishlistItem != null) {
        WishlistGroupSelector(
            item = wishlistItem,
            groups = wishlistGroups,
            onDismiss = { selectorVisible = false },
            onGroupSelectionChange = { groupId, selected -> onGroupSelectionChange(groupId, wishlistItem, selected) },
            onCreateGroupWithPlace = onCreateGroupWithPlace,
        )
    }
}

@Composable
fun PlaceDetailsScreen(
    attraction: Attraction?,
    wishlistItem: WishlistItem?,
    wishlistGroups: List<WishlistGroup>,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onGroupSelectionChange: (Int, WishlistItem, Boolean) -> Unit,
    onCreateGroupWithPlace: (String, WishlistItem) -> Boolean,
    modifier: Modifier = Modifier,
) {
    if (attraction == null) {
        MissingDetails(stringResource(R.string.place_details_not_found), onBackClick, modifier)
        return
    }
    var selectorVisible by remember(wishlistItem?.id) { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.fillMaxSize().background(DetailBackground),
        contentPadding = PaddingValues(bottom = 12.dp),
    ) {
        item {
            DetailsHero(
                name = attraction.name,
                location = attraction.location,
                imageRes = attraction.imageRes,
                categories = attraction.categories,
                onBackClick = onBackClick,
                onShareClick = onShareClick,
            )
        }
        item {
            AboutCard(attraction.name, attraction.description, Modifier.padding(horizontal = 16.dp, vertical = 14.dp))
        }
        item {
            PlaceInformation(attraction, Modifier.padding(horizontal = 16.dp, vertical = 2.dp))
        }
        if (attraction.highlights.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.destination_details_highlights), Modifier.padding(16.dp)) }
            item { HighlightRow(attraction.highlights) }
        }
        if (attraction.tips.isNotEmpty()) {
            item { SectionTitle(stringResource(R.string.destination_details_tips), Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 10.dp)) }
            item { TipsCard(attraction.tips, Modifier.padding(horizontal = 16.dp)) }
        }
        item {
            AddToWishlistButton(
                enabled = wishlistItem != null,
                onClick = { selectorVisible = true },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 22.dp),
            )
        }
        item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }
    if (selectorVisible && wishlistItem != null) {
        WishlistGroupSelector(
            item = wishlistItem,
            groups = wishlistGroups,
            onDismiss = { selectorVisible = false },
            onGroupSelectionChange = { groupId, selected -> onGroupSelectionChange(groupId, wishlistItem, selected) },
            onCreateGroupWithPlace = onCreateGroupWithPlace,
        )
    }
}

@Composable
private fun DetailsHero(
    name: String,
    location: String,
    @DrawableRes imageRes: Int,
    categories: List<String>,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    Box(
        Modifier.fillMaxWidth().aspectRatio(1.48f)
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)),
    ) {
        Image(painterResource(imageRes), name, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color(0xD9000000))),
            ),
        )
        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HeroAction(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.destination_details_back), onBackClick)
            HeroAction(Icons.Default.Share, stringResource(R.string.destination_details_share), onShareClick)
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(16.dp)) {
            Text(name, color = Color.White, fontSize = 31.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(location, color = Color.White, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (categories.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { category ->
                        Surface(color = Color.White.copy(alpha = 0.78f), shape = CircleShape) {
                            Text(category, color = DetailText, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.94f), shadowElevation = 3.dp) {
        IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(icon, description, tint = DetailText, modifier = Modifier.size(25.dp))
        }
    }
}

@Composable
private fun AboutCard(name: String, description: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(name) { mutableStateOf(false) }
    Surface(modifier, shape = DetailShape, color = DetailPaleBlue) {
        Column(Modifier.padding(16.dp).animateContentSize()) {
            Text(stringResource(R.string.destination_details_about, name), color = DetailText, fontSize = 23.sp, lineHeight = 27.sp, fontWeight = FontWeight.Bold)
            Text(
                description,
                color = DetailSecondaryText,
                fontSize = 16.sp,
                lineHeight = 23.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 8.dp),
            )
            if (description.length > 180) {
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)) {
                    Text(
                        stringResource(if (expanded) R.string.destination_details_read_less else R.string.destination_details_read_more),
                        color = DetailBlue,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, color = DetailText, fontSize = 23.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, modifier = modifier)
}

@Composable
private fun PopularPlaceRow(place: Attraction, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DetailOutline),
    ) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painterResource(place.imageRes), place.name,
                Modifier.size(width = 108.dp, height = 76.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(place.name, color = DetailText, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(place.shortDescription, color = DetailSecondaryText, fontSize = 13.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = DetailBlue, modifier = Modifier.size(28.dp))
        }
    }
}

private data class InfoField(val titleRes: Int, val value: String, val icon: ImageVector)

@Composable
private fun PlaceInformation(attraction: Attraction, modifier: Modifier = Modifier) {
    val fields = buildList {
        if (attraction.location.isNotBlank()) add(InfoField(R.string.destination_details_location, attraction.location, Icons.Default.LocationOn))
        attraction.bestTime?.let { add(InfoField(R.string.destination_details_best_time, it, Icons.Default.Schedule)) }
        attraction.entryFee?.let { add(InfoField(R.string.destination_details_entry_fee, it, Icons.Default.ConfirmationNumber)) }
        if (attraction.idealFor.isNotEmpty()) add(InfoField(R.string.destination_details_ideal_for, attraction.idealFor.joinToString(), Icons.Default.Groups))
    }
    if (fields.isEmpty()) return
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = if (maxWidth < 330.dp) 1 else 2
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            fields.chunked(columns).forEach { rowFields ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    rowFields.forEach { field -> InfoCard(field, Modifier.weight(1f)) }
                    repeat(columns - rowFields.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun InfoCard(field: InfoField, modifier: Modifier = Modifier) {
    Surface(modifier.height(126.dp), shape = RoundedCornerShape(15.dp), color = Color.White, border = BorderStroke(1.dp, DetailOutline)) {
        Column(Modifier.padding(13.dp)) {
            Icon(field.icon, null, tint = DetailBlue, modifier = Modifier.size(27.dp))
            Text(stringResource(field.titleRes), color = DetailText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            Text(field.value, color = DetailSecondaryText, fontSize = 13.sp, lineHeight = 17.sp, maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 3.dp))
        }
    }
}

@Composable
private fun HighlightRow(highlights: List<AttractionHighlight>) {
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(highlights, key = AttractionHighlight::title) { highlight ->
            Surface(Modifier.width(174.dp), shape = RoundedCornerShape(15.dp), color = Color.White, border = BorderStroke(1.dp, DetailOutline)) {
                Column {
                    Image(painterResource(highlight.imageRes), highlight.title, Modifier.fillMaxWidth().height(112.dp), contentScale = ContentScale.Crop)
                    Text(highlight.title, color = DetailText, fontWeight = FontWeight.Bold, minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(10.dp))
                }
            }
        }
    }
}

@Composable
private fun TipsCard(tips: List<String>, modifier: Modifier = Modifier) {
    Surface(modifier.fillMaxWidth(), shape = DetailShape, color = DetailPaleBlue) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            tips.forEach { tip ->
                Row {
                    Text("•", color = DetailBlue, fontSize = 22.sp, lineHeight = 20.sp)
                    Text(tip, color = DetailSecondaryText, fontSize = 15.sp, lineHeight = 20.sp, modifier = Modifier.padding(start = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun AddToWishlistButton(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(58.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DetailBlue, contentColor = Color.White),
    ) {
        Icon(Icons.Default.Favorite, null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(10.dp))
        Text(stringResource(R.string.destination_details_add_wishlist), fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MissingDetails(message: String, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().background(DetailBackground).windowInsetsPadding(WindowInsets.statusBars).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        HeroAction(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.destination_details_back), onBackClick)
        Text(message, color = DetailText, style = MaterialTheme.typography.titleLarge)
    }
}
