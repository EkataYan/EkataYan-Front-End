package com.ekatayan.app.ui.businesspartner

import com.ekatayan.app.data.model.BusinessDocument
import com.ekatayan.app.data.model.BusinessPartnerProfile
import com.ekatayan.app.data.model.DocumentType
import com.ekatayan.app.data.model.ListingCategory
import com.ekatayan.app.data.model.ListingStatus
import com.ekatayan.app.viewmodel.BusinessPartnerViewModel

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ekatayan.app.R
import com.ekatayan.app.core.designsystem.theme.*

/** Screen-facing events keep Android pickers and ViewModels out of presentation components. */
class BusinessPartnerActions(
    val updateProfile: ((BusinessPartnerProfile) -> BusinessPartnerProfile) -> Unit,
    val pickBusinessPhotos: () -> Unit,
    val pickProfilePhoto: () -> Unit,
    val removeProfilePhoto: () -> Unit,
    val pickDocument: (DocumentType) -> Unit,
    val removeDocument: (DocumentType) -> Unit,
    val filterListings: (ListingCategory?) -> Unit,
    val deleteListing: (String) -> Unit,
    val changeStatus: (String, ListingStatus) -> Unit,
)

@Composable
fun BusinessPartnerRoute(route: String, listingId: String?, viewModel: BusinessPartnerViewModel,
    onNavigate: (String) -> Unit, onBack: () -> Unit, onHome: () -> Unit, onTab: (String) -> Unit,
    onLoggedIn: () -> Unit, onRestartFlow: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    fun retain(uri: Uri) {
        runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    }
    val businessPhotos = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        uris.forEach(::retain)
        viewModel.updateProfile { it.copy(businessImageUris = (it.businessImageUris + uris.map(Uri::toString)).distinct()) }
    }
    val listingPhotos = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        uris.forEach(::retain)
        viewModel.updateListingDraft { it.copy(imageUris = (it.imageUris + uris.map(Uri::toString)).distinct()) }
    }
    val profilePhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) { retain(uri); viewModel.setProfilePhoto(uri.toString()) }
    }
    val documents = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        viewModel.documentUriPicked(uri?.toString())
    }
    fun launchSafely(action: () -> Unit) { runCatching(action).onFailure { viewModel.pickerError(true) } }
    val request = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
    val actions = BusinessPartnerActions(
        updateProfile = viewModel::updateProfile,
        pickBusinessPhotos = { launchSafely { businessPhotos.launch(request) } },
        pickProfilePhoto = { launchSafely { profilePhoto.launch(request) } },
        removeProfilePhoto = { viewModel.setProfilePhoto(null) },
        pickDocument = { type -> viewModel.selectDocument(type); launchSafely { documents.launch(arrayOf("application/pdf", "image/*", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.oasis.opendocument.text")) } },
        removeDocument = viewModel::removeDocument,
        filterListings = viewModel::filterListings,
        deleteListing = viewModel::deleteListing,
        changeStatus = viewModel::changeStatus,
    )
    val step = route.substringAfter("business_partner/step/", "").toIntOrNull()
    val partnerArea = route in listOf(PARTNER_HOME_ROUTE, PARTNER_LISTINGS_ROUTE, PARTNER_BOOKINGS_ROUTE,
        PARTNER_PROFILE_ROUTE, PARTNER_ANALYTICS_ROUTE, PARTNER_ADD_LISTING_ROUTE, PARTNER_EDIT_PROFILE_ROUTE,
        PARTNER_DETAIL_ROUTE, PARTNER_EDIT_LISTING_ROUTE)
    // Protected destinations require the locally persisted partner session and completed onboarding state.
    if (partnerArea && !state.loggedIn || step == 5 && !state.profile.submitted) {
        LaunchedEffect(route) { onRestartFlow() }
        return
    }
    val editingListing = route == PARTNER_ADD_LISTING_ROUTE || route == PARTNER_EDIT_LISTING_ROUTE
    val editingProfile = route == PARTNER_EDIT_PROFILE_ROUTE
    val back = {
        if (editingListing) viewModel.discardListingDraft()
        if (editingProfile) viewModel.discardProfileEdit()
        when (route) {
            PARTNER_ENTRY_ROUTE -> onHome()
            PARTNER_HOME_ROUTE -> onHome()
            PARTNER_LISTINGS_ROUTE, PARTNER_BOOKINGS_ROUTE, PARTNER_PROFILE_ROUTE -> onTab(PARTNER_HOME_ROUTE)
            else -> onBack()
        }
    }
    BackHandler(onBack = back)
    val editListing: (String) -> Unit = { id -> viewModel.beginListing(id); onNavigate(partnerEditListingRoute(id)) }
    PartnerTheme {
        Scaffold(containerColor = EkataBackground, contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (partnerArea && !editingListing && !editingProfile) PartnerBottomNavigation(route, onTab)
            }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
                when {
                    route == PARTNER_ENTRY_ROUTE -> BusinessPartnerEntryScreen(state.profile.submitted,
                        { viewModel.discardProfileEdit(); onNavigate(partnerStepRoute(1)) }, { onNavigate(PARTNER_LOGIN_ROUTE) }, onHome)
                    step != null -> BusinessPartnerOnboardingScreen(step, state, actions, viewModel.canContinue(step),
                        { if (step == 4) { if (viewModel.submit()) onNavigate(partnerStepRoute(5)) } else onNavigate(partnerStepRoute(step + 1)) },
                        back, { onNavigate(PARTNER_LOGIN_ROUTE) }, onHome)
                    route == PARTNER_LOGIN_ROUTE -> BusinessPartnerLoginScreen(state, viewModel::setLoginEmail, viewModel::setLoginPassword,
                        { if (viewModel.login()) onLoggedIn() }, back)
                    route == PARTNER_HOME_ROUTE -> BusinessPartnerDashboardScreen(state, onNavigate)
                    route == PARTNER_LISTINGS_ROUTE -> PartnerListingsScreen(state, actions, { onNavigate(partnerListingRoute(it)) }, editListing,
                        { viewModel.beginListing(); onNavigate(PARTNER_ADD_LISTING_ROUTE) }, back)
                    editingListing -> state.listingDraft?.let { draft -> PartnerListingFormScreen(draft, viewModel::updateListingDraft,
                        { launchSafely { listingPhotos.launch(request) } }, { if (viewModel.saveListing()) onBack() }, back) }
                    route == PARTNER_DETAIL_ROUTE -> PartnerListingDetailsScreen(listingId.orEmpty(), state.listings, editListing, back,
                        { id -> viewModel.deleteListing(id); onTab(PARTNER_LISTINGS_ROUTE) })
                    route == PARTNER_BOOKINGS_ROUTE -> PartnerBookingsScreen(state, viewModel::filterBookings, viewModel::changeBookingStatus, back)
                    route == PARTNER_PROFILE_ROUTE -> PartnerProfileScreen(state.profile, { viewModel.beginProfileEdit(); onNavigate(PARTNER_EDIT_PROFILE_ROUTE) },
                        actions.pickProfilePhoto, actions.removeProfilePhoto, back,
                        { viewModel.signOut(); onRestartFlow() }, { viewModel.deleteAccount(); onRestartFlow() })
                    editingProfile -> state.profileDraft?.let { PartnerEditProfileScreen(it, actions, { if (viewModel.saveProfile()) onBack() }, back) }
                    route == PARTNER_ANALYTICS_ROUTE -> PartnerAnalyticsScreen(state, back)
                }
            }
        }
        if (state.pickerError) PartnerDialog(stringResource(R.string.bp_picker_error), { viewModel.pickerError(false) }) {
            DialogAction(R.string.bp_done, { viewModel.pickerError(false) })
        }
    }
}

@Composable
private fun PartnerBottomNavigation(route: String, onNavigate: (String) -> Unit) {
    val items = listOf(
        Triple(PARTNER_HOME_ROUTE, R.string.bp_home, Icons.Default.Home),
        Triple(PARTNER_LISTINGS_ROUTE, R.string.bp_listings, Icons.AutoMirrored.Filled.ListAlt),
        Triple(PARTNER_BOOKINGS_ROUTE, R.string.bp_bookings, Icons.Default.CalendarMonth),
        Triple(PARTNER_PROFILE_ROUTE, R.string.bp_nav_profile, Icons.Default.PersonOutline),
    )
    val active = when (route) {
        PARTNER_DETAIL_ROUTE -> PARTNER_LISTINGS_ROUTE
        PARTNER_ANALYTICS_ROUTE -> PARTNER_HOME_ROUTE
        else -> route
    }
    NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
        items.forEach { (target, label, icon) ->
            NavigationBarItem(active == target, { onNavigate(target) }, icon = { Icon(icon, null) }, label = { Text(stringResource(label)) },
                colors = NavigationBarItemDefaults.colors(selectedIconColor = EkataBlue, selectedTextColor = EkataBlue, indicatorColor = EkataLightBlue))
        }
    }
}
