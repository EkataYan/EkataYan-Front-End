package com.ekatayan.app.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.preferencesDataStore

val Context.frontendPreferencesDataStore by preferencesDataStore(
    name = "frontend_preferences",
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, "ekatayan_onboarding")) },
)
