package com.ekatayan.app.data.repository

import com.ekatayan.app.data.local.BookingCatalog
import javax.inject.Inject

class BookingRepository @Inject constructor() {
    fun getPlaces() = BookingCatalog.places
}
