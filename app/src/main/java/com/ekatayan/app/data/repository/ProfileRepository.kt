package com.ekatayan.app.data.repository

import com.ekatayan.app.data.model.ProfileDetails
import javax.inject.Inject

/** Static local demo data; no account service is connected. */
class ProfileRepository @Inject constructor() {
    fun getProfile() = ProfileDetails(
        "Zendaya Holland", "Kandy, Sri Lanka", "zendaya@gmail.com", "12", "24", "4.8", "8",
    )
}
