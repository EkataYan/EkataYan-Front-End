package com.ekatayan.app.feature.businesspartner

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val PARTNER_ENTRY_ROUTE = "business_partner/entry"
const val PARTNER_LOGIN_ROUTE = "business_partner/login"
const val PARTNER_HOME_ROUTE = "business_partner/home"
const val PARTNER_LISTINGS_ROUTE = "business_partner/listings"
const val PARTNER_BOOKINGS_ROUTE = "business_partner/bookings"
const val PARTNER_PROFILE_ROUTE = "business_partner/profile"
const val PARTNER_ANALYTICS_ROUTE = "business_partner/analytics"
const val PARTNER_ADD_LISTING_ROUTE = "business_partner/listing/add"
const val PARTNER_EDIT_PROFILE_ROUTE = "business_partner/profile/edit"
const val PARTNER_DETAIL_ROUTE = "business_partner/listing/{listingId}"
const val PARTNER_EDIT_LISTING_ROUTE = "business_partner/listing/{listingId}/edit"
fun partnerStepRoute(step: Int) = "business_partner/step/$step"
fun partnerListingRoute(id: String) = "business_partner/listing/$id"
fun partnerEditListingRoute(id: String) = "business_partner/listing/$id/edit"

fun NavGraphBuilder.businessPartnerScreens(
    viewModel: BusinessPartnerViewModel,
    onNavigate: (String) -> Unit,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onTab: (String) -> Unit,
    onLoggedIn: () -> Unit,
    onRestartFlow: () -> Unit,
) {
    val routes = listOf(PARTNER_ENTRY_ROUTE, PARTNER_LOGIN_ROUTE, PARTNER_HOME_ROUTE,
        PARTNER_LISTINGS_ROUTE, PARTNER_BOOKINGS_ROUTE, PARTNER_PROFILE_ROUTE, PARTNER_ANALYTICS_ROUTE,
        PARTNER_ADD_LISTING_ROUTE, PARTNER_EDIT_PROFILE_ROUTE) + (1..5).map(::partnerStepRoute)
    routes.forEach { route ->
        composable(route) {
            BusinessPartnerRoute(route, null, viewModel, onNavigate, onBack, onHome, onTab, onLoggedIn, onRestartFlow)
        }
    }
    listOf(PARTNER_DETAIL_ROUTE, PARTNER_EDIT_LISTING_ROUTE).forEach { route ->
        composable(route, arguments = listOf(navArgument("listingId") { type = NavType.StringType })) { entry ->
            BusinessPartnerRoute(route, entry.arguments?.getString("listingId"), viewModel, onNavigate, onBack, onHome, onTab, onLoggedIn, onRestartFlow)
        }
    }
}
