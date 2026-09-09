package com.ekatayan.app.ui.welcome

import android.content.Context

class WelcomePreferences(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )

    fun hasCompletedWelcome(): Boolean = preferences.getBoolean(KEY_WELCOME_COMPLETED, false)

    fun markWelcomeCompleted() {
        preferences.edit().putBoolean(KEY_WELCOME_COMPLETED, true).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "ekatayan_onboarding"
        const val KEY_WELCOME_COMPLETED = "welcome_completed"
    }
}
