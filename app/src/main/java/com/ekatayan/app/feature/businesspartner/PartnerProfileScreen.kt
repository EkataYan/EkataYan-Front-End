package com.ekatayan.app.feature.businesspartner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary

@Composable
fun PartnerBookingsScreen(state: BusinessPartnerState, onFilter: (BookingStatus?) -> Unit,
    onStatus: (String, BookingStatus) -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val bookings = state.bookings.filter { state.bookingFilter == null || it.status == state.bookingFilter }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { PartnerBack(onBack); PartnerHeading(stringResource(R.string.bp_bookings_inquiries)) }
        item { PartnerFilters(listOf(null) + BookingStatus.entries, state.bookingFilter, { stringResource(it?.label ?: R.string.bp_all) }, onFilter) }
        if (bookings.isEmpty()) item { Text(stringResource(if (state.bookings.isEmpty()) R.string.bp_no_bookings else R.string.bp_empty_bookings)) }
        items(bookings, key = { it.id }) { booking ->
            PartnerCard(Modifier.fillMaxWidth()) {
                Text(booking.travellerName, color = PartnerNavy, fontWeight = FontWeight.Bold)
                Text(booking.listingName)
                BookingSchedule(booking)
                Text(stringResource(R.string.bp_amount, money(booking.amount)), color = PartnerNavy)
                Text(stringResource(booking.status.label), color = PartnerNavy)
                Text(booking.message, style = MaterialTheme.typography.bodySmall)
                DialogAction(R.string.bp_view, { selectedId = booking.id })
                FlowRow { BookingActions(booking, onStatus) }
            }
        }
    }
    state.bookings.find { it.id == selectedId }?.let { booking ->
        PartnerDialog(booking.travellerName, { selectedId = null }) {
            Text(booking.listingName)
            BookingSchedule(booking)
            Text(stringResource(R.string.bp_amount, money(booking.amount)))
            Text(booking.message)
            Text(stringResource(booking.status.label))
            FlowRow { BookingActions(booking, onStatus) }
            DialogAction(R.string.bp_done, { selectedId = null })
        }
    }
}

@Composable
private fun BookingSchedule(booking: PartnerBooking) {
    if (booking.bookedOn.isNotBlank()) Text(stringResource(R.string.bp_booking_date, booking.bookedOn), color = EkataTextSecondary)
    Text(stringResource(R.string.bp_service_date, booking.dateLabel), color = EkataTextSecondary)
    Text(stringResource(R.string.bp_booking_quantity, booking.quantity), color = EkataTextSecondary)
}

@Composable
private fun BookingActions(booking: PartnerBooking, onStatus: (String, BookingStatus) -> Unit) {
    if (booking.status == BookingStatus.PENDING) DialogAction(R.string.bp_confirm, { onStatus(booking.id, BookingStatus.CONFIRMED) })
    if (booking.status == BookingStatus.PENDING || booking.status == BookingStatus.CONFIRMED) DialogAction(R.string.bp_decline, { onStatus(booking.id, BookingStatus.CANCELLED) })
    if (booking.status == BookingStatus.CONFIRMED) DialogAction(R.string.bp_complete, { onStatus(booking.id, BookingStatus.COMPLETED) })
}

@Composable
fun PartnerProfileScreen(profile: BusinessPartnerProfile, onEdit: () -> Unit, onPhoto: () -> Unit,
    onRemovePhoto: () -> Unit, onBack: () -> Unit, onSignOut: () -> Unit, onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier) {
    var accountAction by rememberSaveable { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PartnerBack(onBack)
        PartnerHeading(stringResource(R.string.bp_profile))
        PartnerImage(profile.imageUri, modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally), description = profile.businessName)
        TextButton(onPhoto, Modifier.align(Alignment.CenterHorizontally)) { Text(stringResource(R.string.bp_change_photo)) }
        if (profile.imageUri != null) DialogAction(R.string.bp_remove_photo, onRemovePhoto)
        PartnerHeading(profile.businessName, profile.businessType?.let { stringResource(it.label) })
        Text(stringResource(R.string.bp_verification_pending), color = PartnerNavy)
        PartnerCard(Modifier.fillMaxWidth()) {
            Text(profile.email)
            Text(profile.phone)
            Text(profile.address)
            Text(listOf(profile.city, profile.district, profile.country).filter(String::isNotBlank).joinToString(", "))
            Text(profile.locationLabel)
            Text(profile.description)
        }
        PartnerCard(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.bp_owner_details), color = PartnerNavy, fontWeight = FontWeight.SemiBold)
            Text(profile.ownerName)
            Text(profile.contactEmail)
            Text(profile.contactPhone)
        }
        profile.businessImageUris.forEach { PartnerImage(it, modifier = Modifier.fillMaxWidth().height(180.dp)) }
        Text(stringResource(R.string.bp_hours), color = PartnerNavy, fontWeight = FontWeight.Bold)
        profile.businessHours.forEach { day ->
            Row(Modifier.fillMaxWidth()) {
                Text(stringResource(day.day.label), Modifier.weight(1f))
                Text(if (day.open) clockLabel(day.openingMinutes) + " – " + clockLabel(day.closingMinutes) else stringResource(R.string.bp_closed))
            }
        }
        Text(stringResource(R.string.bp_social), color = PartnerNavy, fontWeight = FontWeight.Bold)
        if (profile.socialLinks.none { it.url.isNotBlank() }) Text(stringResource(R.string.bp_no_links))
        profile.socialLinks.filter { it.url.isNotBlank() }.forEach { link ->
            Text(stringResource(link.platform.label), fontWeight = FontWeight.SemiBold)
            Text(link.url)
        }
        Text(stringResource(R.string.bp_account), color = PartnerNavy, fontWeight = FontWeight.Bold)
        PartnerButton(R.string.bp_edit_profile, onEdit)
        PartnerAction(stringResource(R.string.bp_sign_out), Icons.AutoMirrored.Filled.Logout, { accountAction = "sign_out" })
        HorizontalDivider()
        Text(stringResource(R.string.bp_danger_zone), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        OutlinedButton({ accountAction = "delete" }, Modifier.fillMaxWidth(), shape = PartnerShape,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
            Icon(Icons.Default.DeleteOutline, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.bp_delete_account))
        }
    }
    accountAction?.let { action ->
        val deleting = action == "delete"
        PartnerDialog(stringResource(if (deleting) R.string.bp_delete_account_title else R.string.bp_sign_out_title),
            { accountAction = null }) {
            Text(stringResource(if (deleting) R.string.bp_delete_account_message else R.string.bp_sign_out_message))
            if (deleting) Text(stringResource(R.string.bp_delete_account_local), style = MaterialTheme.typography.bodySmall)
            FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                DialogAction(R.string.bp_cancel, { accountAction = null })
                TextButton({ accountAction = null; if (deleting) onDeleteAccount() else onSignOut() }) {
                    Text(stringResource(if (deleting) R.string.bp_delete_account else R.string.bp_sign_out),
                        color = if (deleting) MaterialTheme.colorScheme.error else PartnerNavy)
                }
            }
        }
    }
}

@Composable
fun PartnerEditProfileScreen(profile: BusinessPartnerProfile, actions: BusinessPartnerActions,
    onSave: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        PartnerBack(onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PartnerHeading(stringResource(R.string.bp_edit_profile))
            PartnerImage(profile.imageUri, modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally))
            TextButton(actions.pickProfilePhoto) { Text(stringResource(R.string.bp_change_photo)) }
            if (profile.imageUri != null) DialogAction(R.string.bp_remove_photo, actions.removeProfilePhoto)
            BusinessInformationFields(profile, actions.updateProfile)
            BusinessAdditionalFields(profile, actions.updateProfile, actions.pickBusinessPhotos)
        }
        PartnerButton(R.string.bp_save, onSave, Modifier.padding(20.dp),
            enabled = PartnerValidation.information(profile) && PartnerValidation.details(profile))
    }
}

@Composable
fun PartnerAnalyticsScreen(state: BusinessPartnerState, onBack: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { PartnerBack(onBack); PartnerHeading(stringResource(R.string.bp_analytics), stringResource(R.string.bp_demo_data)) }
        item {
            PartnerCard(Modifier.fillMaxWidth()) {
                Row {
                    PartnerStat(state.profileViews, stringResource(R.string.bp_views), Modifier.weight(1f))
                    PartnerStat(state.inquiries, stringResource(R.string.bp_inquiries), Modifier.weight(1f))
                }
                Row {
                    PartnerStat(state.bookings.size, stringResource(R.string.bp_bookings), Modifier.weight(1f))
                    PartnerStat(state.listings.count { it.status == ListingStatus.ACTIVE }, stringResource(R.string.bp_active_listings), Modifier.weight(1f))
                }
            }
        }
        items(state.listings, key = { it.id }) { listing ->
            PartnerCard(Modifier.fillMaxWidth()) {
                Text(listing.title, color = PartnerNavy, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.bp_listing_metrics, listing.views, listing.inquiries))
                LinearProgressIndicator(progress = { listing.views.toFloat() / state.profileViews.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
