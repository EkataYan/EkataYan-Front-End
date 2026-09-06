package com.ekatayan.app.feature.businesspartner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.*
import com.ekatayan.app.feature.wishlist.WishlistPopupBorder

@Composable
fun BusinessPartnerDashboardScreen(state: BusinessPartnerState, onNavigate: (String) -> Unit, modifier: Modifier = Modifier) {
    var popup by rememberSaveable { mutableStateOf<Int?>(null) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FlightTakeoff, null, tint = EkataBlue)
            Text(stringResource(R.string.bp_brand), Modifier.weight(1f), color = PartnerNavy, fontWeight = FontWeight.Bold, fontSize = 23.sp)
            IconButton({ popup = R.string.bp_notifications }) { Icon(Icons.Default.NotificationsNone, stringResource(R.string.bp_notifications), tint = PartnerNavy) }
            PartnerImage(state.profile.imageUri, modifier = Modifier.size(48.dp).clickable { onNavigate(PARTNER_PROFILE_ROUTE) }, description = stringResource(R.string.bp_profile))
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(stringResource(R.string.bp_welcome), color = EkataTextSecondary)
            PartnerHeading(state.profile.businessName, stringResource(R.string.bp_dashboard_subtitle))
        }
        PartnerCard(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                PartnerStat(state.listings.size, stringResource(R.string.bp_total_listings), Modifier.weight(1f))
                PartnerStat(state.profileViews, stringResource(R.string.bp_views), Modifier.weight(1f))
                PartnerStat(state.inquiries, stringResource(R.string.bp_inquiries), Modifier.weight(1f))
            }
        }
        val quickActions = listOf(
            Triple(R.string.bp_manage_listings, Icons.AutoMirrored.Filled.ListAlt, PARTNER_LISTINGS_ROUTE),
            Triple(R.string.bp_bookings_inquiries, Icons.Default.CalendarMonth, PARTNER_BOOKINGS_ROUTE),
            Triple(R.string.bp_profile, Icons.Default.Storefront, PARTNER_PROFILE_ROUTE),
            Triple(R.string.bp_analytics, Icons.Default.BarChart, PARTNER_ANALYTICS_ROUTE),
        )
        quickActions.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEach { (label, icon, route) ->
                    PartnerCard(Modifier.weight(1f).heightIn(min = 126.dp), { onNavigate(route) }) {
                        Icon(icon, null, Modifier.align(Alignment.CenterHorizontally).size(36.dp), tint = EkataBlue)
                        Text(stringResource(label), Modifier.align(Alignment.CenterHorizontally), color = PartnerNavy, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        PartnerCard(Modifier.fillMaxWidth(), { popup = R.string.bp_help }) {
            Text(stringResource(R.string.bp_help), color = PartnerNavy, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.bp_help_subtitle), color = EkataTextSecondary)
        }
        Text(stringResource(R.string.bp_demo_data), color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall)
    }
    popup?.let { title -> PartnerDialog(stringResource(title), { popup = null }) {
        Text(stringResource(if (title == R.string.bp_help) R.string.bp_help_message else R.string.bp_notification_message))
        DialogAction(R.string.bp_done, { popup = null })
    } }
}

@Composable
internal fun PartnerStat(value: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), color = PartnerNavy, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(label, color = EkataTextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun PartnerListingsScreen(state: BusinessPartnerState, actions: BusinessPartnerActions, onView: (String) -> Unit,
    onEdit: (String) -> Unit, onAdd: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    var statusId by rememberSaveable { mutableStateOf<String?>(null) }
    val listings = state.listings.filter { state.listingFilter == null || it.category == state.listingFilter }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PartnerBack(onBack) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.bp_my_listings), Modifier.weight(1f), color = PartnerNavy, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Button(onAdd, shape = PartnerShape) { Text(stringResource(R.string.bp_add_new)) }
            }
        }
        item { PartnerFilters(listOf(null) + ListingCategory.entries, state.listingFilter,
            { stringResource(it?.label ?: R.string.bp_all) }, actions.filterListings) }
        if (listings.isEmpty()) item {
            PartnerCard(Modifier.fillMaxWidth()) {
                Text(stringResource(if (state.listings.isEmpty()) R.string.bp_no_listings else R.string.bp_empty_listings))
                PartnerButton(R.string.bp_first_listing, onAdd)
            }
        }
        items(listings, key = { it.id }) { listing ->
            PartnerListingCard(listing, { onView(listing.id) }, { onEdit(listing.id) }, { statusId = listing.id }, { deleting = listing.id })
        }
    }
    deleting?.let { id -> DeleteListingDialog({ deleting = null }, { actions.deleteListing(id); deleting = null }) }
    statusId?.let { id -> PartnerDialog(stringResource(R.string.bp_change_status), { statusId = null }) {
        ListingStatus.entries.forEach { status -> DialogAction(status.label, { actions.changeStatus(id, status); statusId = null }) }
        DialogAction(R.string.bp_cancel, { statusId = null })
    } }
}

@Composable
private fun PartnerListingCard(listing: BusinessListing, onView: () -> Unit, onEdit: () -> Unit, onStatus: () -> Unit, onDelete: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    PartnerCard(Modifier.fillMaxWidth(), onView) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PartnerImage(listing.imageUris.firstOrNull(), listing.imageRes, Modifier.size(width = 90.dp, height = 112.dp), listing.title)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(listing.title, color = PartnerNavy, fontWeight = FontWeight.SemiBold)
                Text(stringResource(listing.category.label), color = EkataTextSecondary, fontSize = 12.sp)
                StatusBadge(listing.status)
                Text(stringResource(if (listing.availability.available) R.string.bp_available else R.string.bp_unavailable), fontSize = 12.sp)
                Text(stringResource(R.string.bp_price_display, money(listing.price), listing.priceUnit), color = EkataTextSecondary, fontSize = 12.sp)
                Text(stringResource(R.string.bp_listing_metrics, listing.views, listing.inquiries), color = EkataTextSecondary, fontSize = 11.sp)
            }
            Box {
                IconButton({ menu = true }, Modifier.size(48.dp)) { Icon(Icons.Default.MoreVert, stringResource(R.string.bp_listing_options), tint = PartnerNavy) }
                DropdownMenu(menu, { menu = false }, containerColor = Color.White, shape = PartnerShape, border = BorderStroke(1.dp, WishlistPopupBorder)) {
                    listOf(R.string.bp_view to onView, R.string.bp_edit to onEdit, R.string.bp_change_status to onStatus, R.string.bp_delete to onDelete).forEach { (label, action) ->
                        DropdownMenuItem({ Text(stringResource(label), color = Color.Black) }, { menu = false; action() })
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerListingFormScreen(draft: ListingDraft, onChange: ((ListingDraft) -> ListingDraft) -> Unit,
    onPhotos: () -> Unit, onSave: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        PartnerBack(onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PartnerHeading(stringResource(if (draft.id == null) R.string.bp_add_listing else R.string.bp_edit_listing))
            PartnerField(draft.title, { value -> onChange { it.copy(title = value) } }, R.string.bp_listing_title)
            Text(stringResource(R.string.bp_listing_category), color = PartnerNavy)
            PartnerFilters(ListingCategory.entries, draft.category, { stringResource(it.label) }, { value ->
                onChange { it.copy(category = value, availability = it.availability.copy(timeSlots = "")) }
            })
            PartnerField(draft.description, { value -> onChange { it.copy(description = value) } }, R.string.bp_description, multiline = true)
            PartnerField(draft.price, { value -> onChange { it.copy(price = value) } }, R.string.bp_price, keyboard = KeyboardType.Decimal,
                error = if (draft.price.isNotEmpty() && draft.price.toDoubleOrNull()?.let { it.isFinite() && it > 0 } != true) stringResource(R.string.bp_price_error) else null)
            PartnerField(draft.priceUnit, { value -> onChange { it.copy(priceUnit = value) } }, R.string.bp_price_unit, hint = stringResource(R.string.bp_price_unit_hint))
            Text(stringResource(R.string.bp_status), color = PartnerNavy)
            PartnerFilters(ListingStatus.entries, draft.status, { stringResource(it.label) }, { value -> onChange { it.copy(status = value) } })
            PartnerField(draft.location, { value -> onChange { it.copy(location = value) } }, R.string.bp_listing_location)
            ListingServiceFields(draft, onChange)
            ListingAvailabilityFields(draft.availability, draft.category == ListingCategory.ACTIVITIES,
                { value -> onChange { it.copy(availability = value) } })
            PartnerField(draft.details, { value -> onChange { it.copy(details = value) } }, R.string.bp_notes, multiline = true)
            Text(stringResource(R.string.bp_listing_images), color = PartnerNavy)
            PhotoStrip(draft.imageUris, onPhotos, { uri -> onChange { it.copy(imageUris = it.imageUris - uri) } }, draft.imageRes, { onChange { it.copy(imageRes = null) } })
            Spacer(Modifier.height(12.dp))
        }
        Column(Modifier.padding(20.dp)) {
            if (!PartnerValidation.listing(draft)) Text(stringResource(R.string.bp_required_hint), style = MaterialTheme.typography.bodySmall)
            PartnerButton(R.string.bp_save, onSave, enabled = PartnerValidation.listing(draft))
        }
    }
}

@Composable
fun PartnerListingDetailsScreen(listingId: String, listings: List<BusinessListing>, onEdit: (String) -> Unit,
    onBack: () -> Unit, onDelete: (String) -> Unit, modifier: Modifier = Modifier) {
    var deleting by rememberSaveable { mutableStateOf(false) }
    val listing = listings.find { it.id == listingId }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PartnerBack(onBack)
        PartnerHeading(stringResource(R.string.bp_listing_details))
        if (listing == null) Text(stringResource(R.string.bp_missing_listing)) else {
            if (listing.imageRes != null) PartnerImage(null, listing.imageRes, Modifier.fillMaxWidth().height(230.dp), listing.title)
            listing.imageUris.forEach { PartnerImage(it, modifier = Modifier.fillMaxWidth().height(230.dp), description = listing.title) }
            PartnerHeading(listing.title, stringResource(listing.category.label))
            StatusBadge(listing.status)
            Text(stringResource(R.string.bp_price_display, money(listing.price), listing.priceUnit), color = PartnerNavy)
            Text(listing.description)
            Text(listing.location)
            PartnerHeading(stringResource(R.string.bp_availability))
            Text(stringResource(if (listing.availability.available) R.string.bp_available else R.string.bp_unavailable))
            Text(if (listing.availability.from.isBlank()) stringResource(R.string.bp_dates_unset)
                else stringResource(R.string.bp_date_range, listing.availability.from, listing.availability.until))
            if (listing.availability.timeSlots.isNotBlank()) Text(listing.availability.timeSlots)
            listing.category.fields.forEach { field ->
                listing.serviceFields[field]?.takeIf(String::isNotBlank)?.let { value ->
                    Text(stringResource(field.label), color = PartnerNavy, fontWeight = FontWeight.SemiBold)
                    Text(if (field.kind == FieldKind.BOOLEAN) stringResource(if (value == "true") R.string.bp_yes else R.string.bp_no) else value)
                }
            }
            if (listing.details.isNotBlank()) Text(listing.details)
            Text(stringResource(R.string.bp_listing_metrics, listing.views, listing.inquiries), color = EkataTextSecondary)
            PartnerButton(R.string.bp_edit_listing, { onEdit(listing.id) })
            DialogAction(R.string.bp_delete, { deleting = true })
        }
    }
    if (deleting && listing != null) DeleteListingDialog({ deleting = false }, { deleting = false; onDelete(listing.id) })
}

@Composable
private fun DeleteListingDialog(onDismiss: () -> Unit, onDelete: () -> Unit) {
    PartnerDialog(stringResource(R.string.bp_delete_title), onDismiss) {
        Text(stringResource(R.string.bp_delete_message))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DialogAction(R.string.bp_cancel, onDismiss)
            DialogAction(R.string.bp_delete, onDelete)
        }
    }
}

@Composable
private fun ListingServiceFields(draft: ListingDraft, onChange: ((ListingDraft) -> ListingDraft) -> Unit) {
    if (draft.category.fields.isNotEmpty()) PartnerHeading(stringResource(R.string.bp_service_fields))
    draft.category.fields.forEach { field ->
        val value = draft.serviceFields[field].orEmpty()
        val update: (String) -> Unit = { text -> onChange { it.copy(serviceFields = it.serviceFields + (field to text)) } }
        if (field.kind == FieldKind.BOOLEAN) {
            Text(stringResource(field.label), color = PartnerNavy)
            PartnerFilters(listOf("", "true", "false"), value,
                { stringResource(when (it) { "true" -> R.string.bp_yes; "false" -> R.string.bp_no; else -> R.string.bp_not_set }) }, update)
        } else PartnerField(value, update, field.label,
            keyboard = when (field.kind) {
                FieldKind.COUNT, FieldKind.QUANTITY -> KeyboardType.Number
                FieldKind.MONEY -> KeyboardType.Decimal
                else -> KeyboardType.Text
            },
            multiline = field in listOf(ListingField.AMENITIES, ListingField.CANCELLATION, ListingField.MENU,
                ListingField.INCLUDED, ListingField.EXCLUDED, ListingField.RENTAL_CONDITIONS),
            error = if (field.isValid(value)) null else stringResource(when (field.kind) {
                FieldKind.TIME -> R.string.bp_time_error
                FieldKind.MONEY, FieldKind.QUANTITY -> R.string.bp_nonnegative_error
                else -> R.string.bp_number_error
            }))
    }
}

@Composable
private fun ListingAvailabilityFields(value: ListingAvailability, showTimeSlots: Boolean, onChange: (ListingAvailability) -> Unit) {
    PartnerHeading(stringResource(R.string.bp_availability))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(if (value.available) R.string.bp_available else R.string.bp_unavailable), Modifier.weight(1f))
        Switch(value.available, { onChange(value.copy(available = it)) })
    }
    Text(stringResource(R.string.bp_availability_hint), style = MaterialTheme.typography.bodySmall)
    PartnerField(value.from, { onChange(value.copy(from = it)) }, R.string.bp_available_from)
    PartnerField(value.until, { onChange(value.copy(until = it)) }, R.string.bp_available_until)
    if (showTimeSlots) PartnerField(value.timeSlots, { onChange(value.copy(timeSlots = it)) }, R.string.bp_time_slots)
    if (!value.isValid()) Text(stringResource(R.string.bp_availability_error), color = MaterialTheme.colorScheme.error)
}
