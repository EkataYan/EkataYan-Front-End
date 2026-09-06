package com.ekatayan.app.feature.businesspartner

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.ekatayan.app.MainActivity
import com.ekatayan.app.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Exercises the real application NavHost. Picker results are supplied as local fixtures. */
class BusinessPartnerFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun click(text: String) = compose.onNodeWithText(text).performScrollTo().performClick()
    private fun input(label: String, value: String) = compose.onNodeWithText(label).performScrollTo().performTextReplacement(value)

    @Test fun travellerBranchesOnboardingAndPartnerWorkspace() {
        compose.waitUntil(15_000) { compose.onAllNodesWithText("Log In").fetchSemanticsNodes().isNotEmpty() }
        click("Log In")
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Partnership").fetchSemanticsNodes().isNotEmpty() }
        click("Partnership")
        click("I'm a Traveller")
        compose.onNodeWithText("Sorry, you are not allowed to access this page.").assertIsDisplayed()
        compose.onNodeWithText("Go Back").performClick()
        compose.onNodeWithText("I'm a Business Partner").assertIsDisplayed()
        click("I'm a Traveller")
        compose.onNodeWithText("Cancel").performClick()
        compose.onNodeWithText("Recommended For You").assertExists()
        click("Partnership")
        click("I'm a Business Partner")
        compose.onNodeWithText("Next").assertIsNotEnabled()
        click("Hotel / Accommodation")
        compose.onNodeWithText("Next").performClick()
        input("Business Name *", "Sunrise Hotel")
        input("Business Email *", "owner@example.com")
        input("Phone Number *", "+94771234567")
        input("Business Address *", "Beach Road, Galle")
        input("City *", "Galle")
        input("District *", "Galle")
        input("Country *", "Sri Lanka")
        input("Owner / Manager name *", "Amara Perera")
        input("Contact email *", "contact@example.com")
        input("Contact phone number *", "+94771234567")
        click("Add Location")
        input("Location label", "Galle, Sri Lanka")
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Next").performClick()
        input("Description *", "A peaceful seaside hotel")
        val vm = ViewModelProvider(compose.activity)[BusinessPartnerViewModel::class.java]
        val image = "android.resource://com.ekatayan.app/${R.drawable.galle}"
        compose.runOnIdle { vm.updateProfile { it.copy(businessImageUris = listOf(image)) } }
        click("Set Business Hours")
        compose.onNodeWithText("Monday").assertExists()
        click("Done")
        click("Add Links")
        compose.onAllNodesWithText("Add")[0].performClick()
        input("Website", "https://example.com")
        click("Done")
        compose.onNodeWithText("Next").performClick()
        compose.runOnIdle {
            DocumentType.entries.filter { it.required }.forEach {
                vm.selectDocument(it)
                vm.documentPicked(BusinessDocument("content://demo/${it.name}", "${it.name}.pdf"))
            }
        }
        compose.onNodeWithText("Submit Application").performClick()
        compose.onNodeWithText("Application Submitted!").assertExists()
        click("Go to Login")
        input("Password", "demo")
        click("Login")
        compose.onNodeWithText("Welcome Back,").assertExists()
        click("Manage Listings")
        click("+ Add New")
        input("Listing title *", "Garden Suite")
        input("Description *", "A quiet family suite")
        input("Price (Rs.) *", "9500")
        input("Price unit *", "night")
        input("Maximum guests", "4")
        input("Units / rooms available", "2")
        input("Available from (YYYY-MM-DD)", "2026-10-01")
        input("Available until (YYYY-MM-DD)", "2026-10-31")
        compose.runOnIdle { vm.updateListingDraft { it.copy(imageUris = listOf(image)) } }
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Garden Suite").assertExists()
        click("Garden Suite")
        compose.onNodeWithText("2026-10-01 to 2026-10-31").assertExists()
        click("Edit Listing")
        input("Listing title *", "Garden Family Suite")
        input("Price (Rs.) *", "11500")
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Garden Family Suite").assertExists()
        click("Delete")
        compose.onNodeWithText("Delete listing?").assertExists()
        compose.onNodeWithText("Delete").performClick()
        compose.onNodeWithText("Garden Family Suite").assertDoesNotExist()
        compose.onNodeWithText("Bookings").performClick()
        compose.onNodeWithText("Bookings & Inquiries").assertExists()
        compose.onAllNodesWithText("Confirm")[0].performClick()
        compose.runOnIdle { assertEquals(BookingStatus.CONFIRMED, vm.uiState.value.bookings.first().status) }
        compose.onNodeWithText("Profile").performClick()
        click("Edit Profile")
        input("Business Name *", "Sunrise Ocean Hotel")
        compose.onNodeWithText("Save").performClick()
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Welcome Back,").assertExists()
        compose.onNodeWithText("Sunrise Ocean Hotel").assertExists()
        click("Analytics")
        compose.onNodeWithText("Active Listings").assertExists()
        compose.onNodeWithText("Home").performClick()
        compose.onNodeWithText("Welcome Back,").assertExists()
        compose.runOnIdle { assertTrue(vm.uiState.value.profile.socialLinks.any { it.url == "https://example.com" }) }
        compose.onNodeWithText("Profile").performClick()
        click("Sign Out")
        compose.onNodeWithText("Sign Out?").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertTrue(vm.uiState.value.loggedIn) }
        click("Sign Out")
        compose.onAllNodesWithText("Sign Out").onLast().performClick()
        compose.onNodeWithText("How would you like to continue?").assertExists()
        compose.runOnIdle { assertTrue(!vm.uiState.value.loggedIn) }
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Recommended For You").assertExists()
        click("Partnership")
        click("Already registered? Go to Login")
        input("Email", "owner@example.com")
        input("Password", "demo")
        click("Login")
        compose.onNodeWithText("Profile").performClick()
        click("Delete Account")
        compose.onNodeWithText("Delete Account?").assertIsDisplayed()
        compose.onNodeWithText("Cancel").performClick()
        compose.runOnIdle { assertTrue(vm.uiState.value.loggedIn) }
        click("Delete Account")
        compose.onAllNodesWithText("Delete Account").onLast().performClick()
        compose.runOnIdle { assertEquals(BusinessPartnerState(), vm.uiState.value) }
        compose.onNodeWithText("How would you like to continue?").assertExists()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithText("Recommended For You").assertExists()

    }
}
