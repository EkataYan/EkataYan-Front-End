package com.ekatayan.app

import android.app.Application
import com.ekatayan.app.data.local.AccountDataIsolation
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EkataYanApplication : Application() {
    @Inject lateinit var accountDataIsolation: AccountDataIsolation

    override fun onCreate() {
        super.onCreate()
        accountDataIsolation.start()
    }
}
