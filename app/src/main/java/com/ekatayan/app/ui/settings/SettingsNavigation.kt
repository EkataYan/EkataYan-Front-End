package com.ekatayan.app.ui.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.ekatayan.app.R

const val SETTINGS_ROUTE = "settings"
const val SETTINGS_ACCOUNT="settings/account"; const val SETTINGS_PASSWORD="settings/password"; const val SETTINGS_APPEARANCE="settings/appearance"; const val SETTINGS_LANGUAGE="settings/language"; const val SETTINGS_PERMISSIONS="settings/permissions"; const val SETTINGS_PRIVACY="settings/privacy"; const val SETTINGS_STORAGE="settings/storage"; const val SETTINGS_HELP="settings/help"; const val SETTINGS_ABOUT="settings/about"; const val SETTINGS_LEGAL="settings/legal"

fun NavGraphBuilder.settingsScreen(
    onLogoutClick: () -> Unit,
    onHomeClick: () -> Unit,
    onTripsClick: () -> Unit,
    onPlannerClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onProfileClick: () -> Unit,
    navigate: (String) -> Unit,
    onBack: () -> Unit,
) {
    composable(SETTINGS_ROUTE) {
        SettingsRoute(
            onLogoutClick = onLogoutClick,
            onHomeClick = onHomeClick,
            onTripsClick = onTripsClick,
            onPlannerClick = onPlannerClick,
            onExpensesClick = onExpensesClick,
            onProfileClick = onProfileClick,
            onAccountClick={navigate(SETTINGS_ACCOUNT)},onPasswordClick={navigate(SETTINGS_PASSWORD)},onAppearanceClick={navigate(SETTINGS_APPEARANCE)},onLanguageClick={navigate(SETTINGS_LANGUAGE)},onPermissionsClick={navigate(SETTINGS_PERMISSIONS)},onPrivacyClick={navigate(SETTINGS_PRIVACY)},onStorageClick={navigate(SETTINGS_STORAGE)},onHelpClick={navigate(SETTINGS_HELP)},onAboutClick={navigate(SETTINGS_ABOUT)},onLegalClick={navigate(SETTINGS_LEGAL)},
        )
    }
    composable(SETTINGS_ACCOUNT){AccountInformationScreen(onBack)}
    composable(SETTINGS_PASSWORD){ChangePasswordScreen(onBack)}
    composable(SETTINGS_PERMISSIONS){PermissionsScreen(onBack)}
    composable(SETTINGS_PRIVACY){PrivacyScreen(onBack)}
    composable(SETTINGS_STORAGE){StorageScreen(onBack)}
    composable(SETTINGS_HELP){HelpScreen(onBack)}
    composable(SETTINGS_ABOUT){AboutScreen(onBack)}
    composable(SETTINGS_LEGAL){LegalScreen(onBack)}
    composable(SETTINGS_APPEARANCE){
        val vm: com.ekatayan.app.viewmodel.SettingsViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
        val state by vm.uiState.collectAsStateWithLifecycle()
        ChoiceScreen(stringResource(R.string.settings_appearance),listOf("system" to stringResource(R.string.settings_theme_system),"light" to stringResource(R.string.settings_theme_light),"dark" to stringResource(R.string.settings_theme_dark)),state.themeMode,{vm.setThemeMode(it)},onBack)
    }
    composable(SETTINGS_LANGUAGE){
        val vm: com.ekatayan.app.viewmodel.SettingsViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()
        val state by vm.uiState.collectAsStateWithLifecycle()
        ChoiceScreen(stringResource(R.string.settings_language),listOf("en" to stringResource(R.string.language_english),"si" to stringResource(R.string.language_sinhala),"ta" to stringResource(R.string.language_tamil)),state.selectedLanguage,vm::setLanguage,onBack)
    }
}

