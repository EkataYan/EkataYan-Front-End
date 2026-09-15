package com.ekatayan.app.core.localization

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLocaleManager {
    const val ENGLISH = "en"
    const val SINHALA = "si"
    const val TAMIL = "ta"

    val supportedLanguages = setOf(ENGLISH, SINHALA, TAMIL)

    fun sanitize(languageTag: String): String =
        languageTag.takeIf(supportedLanguages::contains) ?: ENGLISH

    fun currentLanguage(): String? = AppCompatDelegate.getApplicationLocales()
        .takeUnless { it.isEmpty }
        ?.get(0)
        ?.language
        ?.takeIf(supportedLanguages::contains)

    fun applyLanguage(languageTag: String) {
        val locales = LocaleListCompat.forLanguageTags(sanitize(languageTag))
        if (AppCompatDelegate.getApplicationLocales() != locales) {
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }
}
