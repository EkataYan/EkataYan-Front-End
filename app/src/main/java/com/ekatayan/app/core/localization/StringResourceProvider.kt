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
class StringResourceProvider private constructor(
    private val applicationContext: Context?,
    private val testResolver: ((Int, Array<out Any>) -> String)?,
) {
    @Inject constructor(@ApplicationContext applicationContext: Context) : this(applicationContext, null)

    internal constructor(testResolver: (Int, Array<out Any>) -> String) : this(null, testResolver)

    operator fun get(@StringRes resourceId: Int, vararg arguments: Any): String {
        testResolver?.let { return it(resourceId, arguments) }
        val baseContext = checkNotNull(applicationContext)
        val locales = AppCompatDelegate.getApplicationLocales()
        val context = if (locales.isEmpty) {
            baseContext
        } else {
            val configuration = Configuration(baseContext.resources.configuration).apply {
                setLocales(LocaleList.forLanguageTags(locales.toLanguageTags()))
            }
            baseContext.createConfigurationContext(configuration)
        }
        return context.getString(resourceId, *arguments)
    }
}
