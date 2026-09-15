package com.ekatayan.app.core.localization

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StringResourceProvider @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
) {
    operator fun get(@StringRes resourceId: Int, vararg arguments: Any): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        val context = if (locales.isEmpty) {
            applicationContext
        } else {
            val configuration = Configuration(applicationContext.resources.configuration).apply {
                setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
            }
            applicationContext.createConfigurationContext(configuration)
        }
        return context.getString(resourceId, *arguments)
    }
}
