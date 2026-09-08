package com.ekatayan.app.viewmodel

import com.ekatayan.app.data.repository.ExpensesRepository

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExpensesViewModel @Inject constructor(repository: ExpensesRepository) : ViewModel() {
    val uiState = repository.getSummary()
}
