package com.ekatayan.app.feature.trips

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CreateTripViewModel @Inject constructor(private val repository: TripsRepository) : ViewModel() {
    fun save(name: String, destination: String, startDate: LocalDate, endDate: LocalDate, budget: String, notes: String) = repository.addTrip(name, destination, startDate, endDate, budget, notes)
}
