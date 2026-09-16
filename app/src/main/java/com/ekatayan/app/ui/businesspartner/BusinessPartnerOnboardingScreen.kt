package com.ekatayan.app.ui.businesspartner

import com.ekatayan.app.data.model.BusinessDay
import com.ekatayan.app.data.model.BusinessHours
import com.ekatayan.app.data.model.BusinessPartnerProfile
import com.ekatayan.app.data.model.BusinessPartnerState
import com.ekatayan.app.data.model.BusinessType
import com.ekatayan.app.data.model.DocumentType
import com.ekatayan.app.data.model.PartnerValidation
import com.ekatayan.app.data.model.SocialLink
import com.ekatayan.app.data.model.SocialPlatform

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ekatayan.app.core.designsystem.component.AuthBackdrop
import com.ekatayan.app.core.designsystem.component.AuthBrandLockup
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.EkataBlue
import com.ekatayan.app.core.designsystem.theme.EkataTextSecondary

@Composable
fun BusinessPartnerEntryScreen(submitted: Boolean, onPartner: () -> Unit, onLogin: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    var denied by rememberSaveable { mutableStateOf(false) }
    Box(modifier.fillMaxSize()) {
        AuthBackdrop()
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
            PartnerBack(onHome)
            Column(Modifier.fillMaxWidth().padding(bottom = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.signup_logo), stringResource(R.string.signup_brand),
                    Modifier.size(width = 98.dp, height = 120.dp), contentScale = ContentScale.Fit)
                AuthBrandLockup()
            }
            Column(Modifier.padding(horizontal = 20.dp).widthIn(max = 480.dp).align(Alignment.CenterHorizontally)
                .fillMaxWidth().background(Color.White, PartnerShape).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)) {
                PartnerHeading(stringResource(R.string.bp_entry_title), stringResource(R.string.bp_entry_subtitle))
                PartnerCard(Modifier.fillMaxWidth(), onClick = { denied = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Default.Hiking, null, Modifier.size(52.dp), tint = EkataBlue)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.bp_traveller), color = PartnerNavy, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.bp_traveller_subtitle), color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
                PartnerCard(Modifier.fillMaxWidth(), onClick = onPartner) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Icon(Icons.Default.Storefront, null, Modifier.size(52.dp), tint = EkataBlue)
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.bp_partner), color = PartnerNavy, style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(R.string.bp_partner_subtitle), color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
                if (submitted) TextButton(onLogin) { Text(stringResource(R.string.bp_existing_login)) }
            }
        }
    }
    if (denied) PartnerDialog(stringResource(R.string.bp_access_denied), { denied = false }) {
        Text(stringResource(R.string.bp_denied))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            DialogAction(R.string.bp_go_back, { denied = false })
            DialogAction(R.string.bp_cancel, { denied = false; onHome() })
        }
    }
}

@Composable
fun BusinessPartnerOnboardingScreen(step: Int, state: BusinessPartnerState, actions: BusinessPartnerActions,
    canContinue: Boolean, onNext: () -> Unit, onBack: () -> Unit, onLogin: () -> Unit, onHome: () -> Unit,
    modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        PartnerBack(onBack)
        if (step == 5) {
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                val availableHeight = maxHeight
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .heightIn(min = availableHeight).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically)) {
                    Text(stringResource(R.string.bp_step, step), color = EkataBlue,
                        modifier = Modifier.align(Alignment.CenterHorizontally))
                    Icon(Icons.Default.Verified, null, Modifier.size(108.dp).align(Alignment.CenterHorizontally), tint = EkataBlue)
                    Text(stringResource(R.string.bp_submitted), Modifier.fillMaxWidth(), color = PartnerNavy,
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)
                    Text(stringResource(R.string.bp_submitted_message), color = EkataTextSecondary,
                        style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.bp_demo_submission), color = EkataTextSecondary,
                        style = MaterialTheme.typography.bodySmall)
                    PartnerButton(R.string.bp_go_login, onLogin)
                    TextButton(onHome, Modifier.align(Alignment.CenterHorizontally)) { Text(stringResource(R.string.bp_back_home)) }
                }
            }
        } else {
            Text(stringResource(R.string.bp_step, step), Modifier.padding(horizontal = 20.dp), color = EkataBlue)
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when (step) {
                    1 -> {
                        PartnerHeading(stringResource(R.string.bp_category_title), stringResource(R.string.bp_category_subtitle))
                        BusinessType.entries.forEach { type ->
                            PartnerAction(stringResource(type.label), when (type) {
                                BusinessType.HOTEL -> Icons.Default.Hotel
                                BusinessType.RESTAURANT -> Icons.Default.Restaurant
                                BusinessType.TRANSPORT -> Icons.Default.DirectionsBus
                                BusinessType.TOURS -> Icons.Default.Explore
                                BusinessType.VACATION -> Icons.Default.House
                                BusinessType.GEAR, BusinessType.EQUIPMENT -> Icons.Default.Backpack
                                BusinessType.OTHER -> Icons.Default.GridView
                            }, { actions.updateProfile { it.copy(businessType = type) } }, state.profile.businessType == type)
                        }
                    }
                    2 -> {
                        PartnerHeading(stringResource(R.string.bp_information), stringResource(R.string.bp_information_subtitle))
                        BusinessInformationFields(state.profile, actions.updateProfile)
                    }
                    3 -> {
                        PartnerHeading(stringResource(R.string.bp_details), stringResource(R.string.bp_details_subtitle))
                        Text(stringResource(R.string.bp_logo), color = PartnerNavy)
                        PartnerImage(state.profile.imageUri, modifier = Modifier.size(100.dp))
                        TextButton(actions.pickProfilePhoto) { Text(stringResource(R.string.bp_change_photo)) }
                        if (state.profile.imageUri != null) DialogAction(R.string.bp_remove_photo, actions.removeProfilePhoto)
                        BusinessAdditionalFields(state.profile, actions.updateProfile, actions.pickBusinessPhotos)
                    }
                    4 -> {
                        PartnerHeading(stringResource(R.string.bp_verification), stringResource(R.string.bp_verification_subtitle))
                        PartnerCard(Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.bp_required_docs), color = PartnerNavy, style = MaterialTheme.typography.titleMedium)
                            DocumentType.entries.forEach { type ->
                                val document = state.documents[type]
                                Text(stringResource(type.label), color = PartnerNavy)
                                document?.let { Text(it.filename, style = MaterialTheme.typography.bodySmall) }
                                Row {
                                    TextButton({ actions.pickDocument(type) }) { Text(stringResource(if (document == null) R.string.bp_upload else R.string.bp_replace)) }
                                    if (document != null) DialogAction(R.string.bp_remove, { actions.removeDocument(type) })
                                }
                                HorizontalDivider(color = com.ekatayan.app.core.designsystem.theme.EkataLightBlue)
                            }
                        }
                        Text(stringResource(R.string.bp_privacy_note), color = EkataTextSecondary)
                    }

                }
            }
            if (step < 5) Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                if (!canContinue) Text(stringResource(R.string.bp_required_hint), color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall)
                PartnerButton(if (step == 4) R.string.bp_submit else R.string.bp_next, onNext, enabled = canContinue)
            }
        }
    }
}

@Composable
internal fun BusinessInformationFields(profile: BusinessPartnerProfile, onChange: ((BusinessPartnerProfile) -> BusinessPartnerProfile) -> Unit) {
    var location by rememberSaveable { mutableStateOf(false) }
    PartnerField(profile.businessName, { value -> onChange { it.copy(businessName = value) } }, R.string.bp_name)
    Text(stringResource(R.string.bp_business_category), color = PartnerNavy)
    PartnerFilters(BusinessType.entries, profile.businessType, { stringResource(it?.label ?: R.string.bp_not_set) },
        { value -> onChange { it.copy(businessType = value) } })
    PartnerField(profile.email, { value -> onChange { it.copy(email = value) } }, R.string.bp_email, keyboard = KeyboardType.Email,
        error = if (profile.email.isNotEmpty() && !PartnerValidation.email(profile.email)) stringResource(R.string.bp_email_error) else null)
    PartnerField(profile.phone, { value -> onChange { it.copy(phone = value) } }, R.string.bp_phone, keyboard = KeyboardType.Phone, hint = "+94",
        error = if (profile.phone.isNotEmpty() && !PartnerValidation.phone(profile.phone)) stringResource(R.string.bp_phone_error) else null)
    PartnerField(profile.address, { value -> onChange { it.copy(address = value) } }, R.string.bp_address)
    PartnerField(profile.city, { value -> onChange { it.copy(city = value) } }, R.string.bp_city)
    PartnerField(profile.district, { value -> onChange { it.copy(district = value) } }, R.string.bp_district)
    PartnerField(profile.country, { value -> onChange { it.copy(country = value) } }, R.string.bp_country)
    PartnerHeading(stringResource(R.string.bp_owner_details))
    PartnerField(profile.ownerName, { value -> onChange { it.copy(ownerName = value) } }, R.string.bp_owner_name)
    PartnerField(profile.contactEmail, { value -> onChange { it.copy(contactEmail = value) } }, R.string.bp_contact_email,
        keyboard = KeyboardType.Email,
        error = if (profile.contactEmail.isNotEmpty() && !PartnerValidation.email(profile.contactEmail)) stringResource(R.string.bp_email_error) else null)
    PartnerField(profile.contactPhone, { value -> onChange { it.copy(contactPhone = value) } }, R.string.bp_contact_phone,
        keyboard = KeyboardType.Phone,
        error = if (profile.contactPhone.isNotEmpty() && !PartnerValidation.phone(profile.contactPhone)) stringResource(R.string.bp_phone_error) else null)
    Text(stringResource(R.string.bp_location), color = PartnerNavy)
    PartnerAction(profile.locationLabel.ifBlank { stringResource(R.string.bp_add_location) }, Icons.Default.LocationOn, { location = true })
    if (location) {
        var label by rememberSaveable { mutableStateOf(profile.locationLabel) }
        PartnerDialog(stringResource(R.string.bp_add_location), { location = false }) {
            Text(stringResource(R.string.bp_location_demo))
            PartnerField(label, { label = it }, R.string.bp_location_label, hint = stringResource(R.string.bp_location_hint))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                DialogAction(R.string.bp_cancel, { location = false })
                DialogAction(R.string.bp_save, { onChange { it.copy(locationLabel = label.trim()) }; location = false }, label.isNotBlank())
            }
        }
    }
}

@Composable
internal fun BusinessAdditionalFields(profile: BusinessPartnerProfile, onChange: ((BusinessPartnerProfile) -> BusinessPartnerProfile) -> Unit, onPickPhotos: () -> Unit) {
    var hours by rememberSaveable { mutableStateOf(false) }
    var links by rememberSaveable { mutableStateOf(false) }
    PartnerField(profile.description, { value -> onChange { it.copy(description = value.take(500)) } }, R.string.bp_description,
        multiline = true, hint = stringResource(R.string.bp_description_hint))
    Text(stringResource(R.string.bp_count, profile.description.length), color = EkataTextSecondary)
    Text(stringResource(R.string.bp_images), color = PartnerNavy)
    Text(stringResource(R.string.bp_images_hint), color = EkataTextSecondary, style = MaterialTheme.typography.bodySmall)
    PhotoStrip(profile.businessImageUris, onPickPhotos, { uri -> onChange { it.copy(businessImageUris = it.businessImageUris - uri) } })
    Text(stringResource(R.string.bp_hours), color = PartnerNavy)
    PartnerAction(stringResource(R.string.bp_set_hours), Icons.Default.Schedule, { hours = true })
    Text(stringResource(R.string.bp_social), color = PartnerNavy)
    PartnerAction(stringResource(R.string.bp_add_links), Icons.Default.Link, { links = true })
    if (hours) BusinessHoursDialog(profile.businessHours, { values -> onChange { it.copy(businessHours = values) } }, { hours = false })
    if (links) SocialLinksDialog(profile.socialLinks, { values -> onChange { it.copy(socialLinks = values) } }, { links = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BusinessHoursDialog(hours: List<BusinessHours>, onChange: (List<BusinessHours>) -> Unit, onDismiss: () -> Unit) {
    var selectedDay by remember { mutableStateOf<BusinessDay?>(null) }
    var opening by remember { mutableStateOf(true) }
    if (selectedDay == null) PartnerDialog(stringResource(R.string.bp_hours), onDismiss) {
        hours.forEach { day ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(day.day.label), Modifier.weight(1f))
                Text(stringResource(if (day.open) R.string.bp_open else R.string.bp_closed))
                Switch(day.open, { value -> onChange(hours.map { if (it.day == day.day) it.copy(open = value) else it }) })
            }
            if (day.open) Row {
                TextButton({ selectedDay = day.day; opening = true }) { Text(stringResource(R.string.bp_time_value, stringResource(R.string.bp_opening), clockLabel(day.openingMinutes)), color = Color.Black) }
                TextButton({ selectedDay = day.day; opening = false }) { Text(stringResource(R.string.bp_time_value, stringResource(R.string.bp_closing), clockLabel(day.closingMinutes)), color = Color.Black) }
            }
        }
        Text(stringResource(R.string.bp_overnight), style = MaterialTheme.typography.bodySmall)
        DialogAction(R.string.bp_done, onDismiss)
    } else {
        val day = hours.first { it.day == selectedDay }
        val minutes = if (opening) day.openingMinutes else day.closingMinutes
        val time = rememberTimePickerState(minutes / 60, minutes % 60, is24Hour = true)
        PartnerDialog(stringResource(if (opening) R.string.bp_opening else R.string.bp_closing), { selectedDay = null }) {
            TimeInput(time, colors = TimePickerDefaults.colors(timeSelectorSelectedContentColor = Color.Black, timeSelectorUnselectedContentColor = Color.Black))
            Row {
                DialogAction(R.string.bp_cancel, { selectedDay = null })
                DialogAction(R.string.bp_save, {
                    val value = time.hour * 60 + time.minute
                    onChange(hours.map { if (it.day == selectedDay) { if (opening) it.copy(openingMinutes = value) else it.copy(closingMinutes = value) } else it })
                    selectedDay = null
                })
            }
        }
    }
}

@Composable
private fun SocialLinksDialog(links: List<SocialLink>, onChange: (List<SocialLink>) -> Unit, onDismiss: () -> Unit) {
    PartnerDialog(stringResource(R.string.bp_social), onDismiss) {
        SocialPlatform.entries.forEach { platform ->
            val link = links.find { it.platform == platform }
            if (link == null) Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(platform.label), Modifier.weight(1f))
                DialogAction(R.string.bp_add, { onChange(links + SocialLink(platform, "")) })
            } else {
                PartnerField(link.url, { value -> onChange(links.map { if (it.platform == platform) it.copy(url = value) else it }) }, platform.label, keyboard = KeyboardType.Uri)
                DialogAction(R.string.bp_remove, { onChange(links.filterNot { it.platform == platform }) })
            }
        }
        DialogAction(R.string.bp_done, { onChange(links.filter { it.url.isNotBlank() }); onDismiss() })
    }
}

@Composable
fun BusinessPartnerLoginScreen(state: BusinessPartnerState, onEmail: (String) -> Unit, onPassword: (String) -> Unit,
    onLogin: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PartnerBack(onBack)
        Image(painterResource(R.drawable.signup_logo), stringResource(R.string.bp_brand), Modifier.align(Alignment.CenterHorizontally).size(140.dp))
        PartnerHeading(stringResource(R.string.bp_login_title), stringResource(R.string.bp_demo_login))
        PartnerField(state.loginEmail, onEmail, R.string.bp_login_email, keyboard = KeyboardType.Email,
            error = if (state.loginEmail.isNotEmpty() && !PartnerValidation.email(state.loginEmail)) stringResource(R.string.bp_email_error) else null)
        PartnerField(state.loginPassword, onPassword, R.string.bp_password, keyboard = KeyboardType.Password, password = true)
        PartnerButton(R.string.bp_login, onLogin, enabled = PartnerValidation.email(state.loginEmail) && state.loginPassword.isNotBlank())
    }
}
